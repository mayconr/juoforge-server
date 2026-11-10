# Network transports

`NetworkServer` defines the lifecycle (`start()` and `close()`). The shard selects
the transport through `NetworkBootstrap.build(NetworkSettings)`. `NetworkSettings`
is separate from gameplay and content files.

| Mode | Implementation | Communication |
| --- | --- | --- |
| `standalone` | `ServerStartup` | Listens directly on TCP at the configured host/port. |
| `gateway` | `GatewayNetworkServer` | Connects to the tunnel and registers the shard. No game TCP listener. |

Both use `UOChannelInitializer`, `NettySessionManager`, and the same handlers and
rules. The gateway adapter creates an in-memory `LocalChannel` pair per session,
connecting the byte stream to the UO pipeline. There is no intermediate local TCP
connection. Gateway types stay in `infrastructure.gateway`, outside the domain
and game session interfaces.

## Configuration

The server is a library. The shard entry point loads properties and starts the
network explicitly:

```java
NetworkSettings network = NetworkSettings.from(System.getProperties());
NetworkServer server = new NetworkBootstrap(runtime).build(network);
Runtime.getRuntime().addShutdownHook(new Thread(server::close));
server.start().toCompletableFuture().join();
```

Standalone:

```properties
network.mode=standalone
network.host=0.0.0.0
network.port=2593
```

Gateway:

```properties
network.mode=gateway
network.host=127.0.0.1
network.port=2594
network.gateway.shard=default
```

Also supply `network.gateway.token`, matching the secret configured in the gateway.
The shard can populate this property from `System.getenv("GATEWAY_SHARD_TOKEN")`
before calling `NetworkSettings.from`. The token is required in this mode and
omitted from `NetworkSettings.toString()`. A file can also be loaded with
`Properties.load`; the library does not load files or environment variables implicitly.

`network.host`/`network.port` specify the bind address in standalone mode and the
tunnel destination in gateway mode. Defaults are `0.0.0.0:2593` and `127.0.0.1:2594`.
In the gateway, `routes.2593: default` routes public port 2593 to shard `default`.
The consulted gateway forwards bytes and does not implement UO login; the server's
login conditions and version support still apply.

The mode is required: missing or blank `network.mode` produces an error.
The bootstrap does not provide a no-argument `build()`. Migrate the legacy call
`new NetworkBootstrap(runtime).build().bindAsync(port)` to an explicit server mode:

```java
NetworkServer server = new NetworkBootstrap(runtime)
        .build(NetworkSettings.standalone("0.0.0.0", port));
server.start();
```

## Lifecycle and protocol

`start()` is nonblocking and idempotent. Completion indicates a successful bind
in standalone mode or receipt of `REGISTER_ACK` in gateway mode. While the gateway
is unavailable, the client retries with backoff from 250 ms to 30 s. Losing the
tunnel closes its sessions; reconnection registers the shard again without
restoring previous sessions. `close()` is idempotent and starts asynchronous
shutdown of owned channels and event loops.

The adapter follows the `juoforge-gateway` v1 protocol:
`protocol/TunnelFrameDecoder`, `TunnelFrameEncoder`, `TunnelPipeline`, and
`client/GatewayTunnelClient`, `TunnelSessionManager`. Frames contain a 4-byte
length, 1-byte version and type, an 8-byte session ID, and payload; integers are
big endian. Registration contains `shard + NUL + token` in UTF-8.
There are heartbeats, registration/idle timeouts, and per-session limits. Large
responses are split into frames. Limits follow `TunnelSettings.defaults()`:
64 KiB frames, a 256 KiB buffer, 4096 sessions, a 10 s heartbeat, and 35 s idle timeout.

The transport was adapted for Netty 4.1/Java 21 within the server infrastructure.
There is no Maven dependency on the gateway application (Java 25/Spring Boot 4).
Future protocol changes must be reconciled with this adapter.

## ClassicUO compatibility

Verified in `docs/references/ClassicUO/src/ClassicUO.Client/`:

- `Game/Scenes/LoginScene.cs`, `HandleRelayServerPacket`: after relay, enables
  compression and sends the 4-byte seed before the second login.
- `Network/OutgoingPackets.cs`, `Send_SecondLogin`: sends `0x91`, the seed, and
  30-byte username and password fields.
- `Network/PacketsTable.cs`, table and constructor: distinguishes fixed and
  variable lengths and adjusts some based on version, including item drop.

The tunnel preserves the byte stream and existing Huffman compression. The shared
decoder accumulates the seed and incomplete packets before interpreting them;
tunnel frame boundaries are not UO packet boundaries. The table follows formats
already implemented by the server, including 15-byte item drop; it does not extend
support for older clients. The protocol does not transmit the player's original
IP address: gateway sessions receive a logical local address.

`GatewayNetworkTest` uses a TCP peer with an independent frame implementation to
verify registration, interleaved sessions, fragmentation, compressed responses,
isolated closing, and reconnection without starting the gateway's Spring application.
