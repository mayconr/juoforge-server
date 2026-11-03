package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.infrastructure.gateway.GatewayNetworkServer;
import com.github.mayconr.juoserver.infrastructure.server.Huffman;
import com.github.mayconr.juoserver.infrastructure.server.ServerStartup;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.server.UOProtocolDecoder;
import com.github.mayconr.juoserver.infrastructure.server.UOProtocolEncoder;
import com.github.mayconr.juoserver.network.packet.PingPong;
import io.netty.channel.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GatewayNetworkTest {
    @Test
    void bootstrapSelectsTransportWithoutStartingGatewayForStandalone() {
        ServerRuntime runtime = mock(ServerRuntime.class);
        when(runtime.world()).thenReturn(mock(com.github.mayconr.juoserver.game.world.World.class,
                withSettings().extraInterfaces(WorldInternal.class)));
        NetworkBootstrap bootstrap = new NetworkBootstrap(runtime);
        try (var standalone = bootstrap.build(NetworkSettings.standalone("0.0.0.0", 2593));
             var gateway = bootstrap.build(NetworkSettings.gateway("127.0.0.1", 2594, "default", "secret"))) {
            assertInstanceOf(ServerStartup.class, standalone);
            assertInstanceOf(GatewayNetworkServer.class, gateway);
        }
    }

    @Test
    void configurationSelectsTransportAndHidesCredentials() {
        Properties properties = new Properties();
        assertThrows(IllegalArgumentException.class, () -> NetworkSettings.from(properties));
        properties.setProperty("network.mode", " ");
        assertThrows(IllegalArgumentException.class, () -> NetworkSettings.from(properties));
        properties.setProperty("network.mode", "standalone");
        assertEquals(NetworkSettings.Mode.STANDALONE, NetworkSettings.from(properties).mode());
        assertEquals(2593, NetworkSettings.from(properties).port());
        properties.setProperty("network.mode", "gateway");
        assertThrows(IllegalArgumentException.class, () -> NetworkSettings.from(properties));
        properties.setProperty("network.gateway.token", "secret");
        NetworkSettings gateway = NetworkSettings.from(properties);
        assertEquals(NetworkSettings.Mode.GATEWAY, gateway.mode());
        assertEquals(2594, gateway.port());
        assertFalse(gateway.toString().contains("secret"));
    }

    @Test
    void multiplexesFragmentedUoStreamsAndReconnectsAfterTunnelLoss() throws Exception {
        BlockingQueue<Channel> gameChannels = new LinkedBlockingQueue<>();
        ChannelInitializer<Channel> pipeline = new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(new UOProtocolDecoder(), new UOProtocolEncoder(),
                        new SimpleChannelInboundHandler<PingPong>() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                gameChannels.add(ctx.channel());
                            }

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, PingPong ping) {
                                ctx.writeAndFlush(ping);
                            }
                        });
            }
        };
        try (ServerSocket gateway = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
             GatewayNetworkServer server = new GatewayNetworkServer(
                     NetworkSettings.gateway("127.0.0.1", gateway.getLocalPort(), "default", "secret"), pipeline)) {
            gateway.setSoTimeout(5000);
            CompletionStage<Void> ready = server.start();
            try (Socket tunnel = gateway.accept()) {
                tunnel.setSoTimeout(5000);
                DataInputStream input = new DataInputStream(tunnel.getInputStream());
                DataOutputStream output = new DataOutputStream(tunnel.getOutputStream());
                Frame registration = read(input);
                assertEquals(0, registration.type);
                assertEquals(0, registration.id);
                assertEquals("default\0secret", new String(registration.data, StandardCharsets.UTF_8));
                write(output, 1, 0, new byte[0]); // REGISTER_ACK
                ready.toCompletableFuture().get(5, TimeUnit.SECONDS);

                write(output, 2, 101, new byte[0]);
                Frame firstAccepted = read(input);
                assertEquals(3, firstAccepted.type);
                assertEquals(101, firstAccepted.id);
                write(output, 2, 202, new byte[0]);
                Frame accepted = read(input);
                assertEquals(3, accepted.type);
                assertEquals(202, accepted.id);
                Channel first = gameChannels.poll(5, TimeUnit.SECONDS);
                Channel second = gameChannels.poll(5, TimeUnit.SECONDS);
                assertNotNull(first);
                assertNotNull(second);

                // The four-byte game seed and fixed-length ping can cross arbitrary DATA boundaries.
                write(output, 6, 101, new byte[]{1});
                write(output, 6, 202, new byte[]{5, 6, 7, 8, 0x73, 22});
                write(output, 6, 101, new byte[]{2, 3, 4, 0x73});
                Frame response = read(input);
                assertEquals(6, response.type);
                assertEquals(202, response.id);
                assertArrayEquals(new Huffman().encode(new byte[]{0x73, 22}), response.data);
                write(output, 6, 101, new byte[]{11});
                response = read(input);
                assertEquals(101, response.id);
                assertArrayEquals(new Huffman().encode(new byte[]{0x73, 11}), response.data);

                write(output, 5, 101, new byte[0]); // remote close affects only one session
                first.closeFuture().await(5, TimeUnit.SECONDS);
                assertFalse(first.isActive());
                assertTrue(second.isActive());
                second.close().sync();
                Frame closed = read(input);
                assertEquals(5, closed.type);
                assertEquals(202, closed.id);
            }
            // A fresh tunnel registers again; old logical sessions are not resumed.
            try (Socket reconnect = gateway.accept()) {
                reconnect.setSoTimeout(5000);
                assertEquals(0, read(new DataInputStream(reconnect.getInputStream())).type);
                write(new DataOutputStream(reconnect.getOutputStream()), 1, 0, new byte[0]);
            }
        }
    }

    private record Frame(int type, long id, byte[] data) {}

    /** Independent wire implementation: int length, byte version/type, long session, payload. */
    private static Frame read(DataInputStream input) throws IOException {
        int size = input.readInt();
        assertEquals(1, input.readUnsignedByte());
        int type = input.readUnsignedByte();
        long id = input.readLong();
        byte[] data = input.readNBytes(size - 10);
        assertEquals(size - 10, data.length);
        return new Frame(type, id, data);
    }

    private static void write(DataOutputStream output, int type, long id, byte[] data) throws IOException {
        output.writeInt(10 + data.length);
        output.writeByte(1);
        output.writeByte(type);
        output.writeLong(id);
        output.write(data);
        output.flush();
    }
}
