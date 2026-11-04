package com.github.mayconr.juoserver.infrastructure.gateway.client;

import com.github.mayconr.juoserver.infrastructure.gateway.protocol.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;

/** Owns its EventLoop; start/close are nonblocking and safe from callbacks. */
public final class GatewayTunnelClient implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(GatewayTunnelClient.class);
  private final String host, shard, token;
  private final int port;
  private final TunnelSettings settings;
  private final TunnelSessionListener listener;
  private final EventLoopGroup loops;
  private final CompletableFuture<Void> ready = new CompletableFuture<>();
  private final CompletableFuture<Void> stopped = new CompletableFuture<>();
  private volatile boolean closed, registered;
  private boolean started;
  private Channel channel;
  private long delay;
  private ScheduledFuture<?> retry;

  public GatewayTunnelClient(
      String host,
      int port,
      String shard,
      String token,
      TunnelSettings settings,
      TunnelSessionListener listener) {
    if (shard.isBlank()
        || shard.indexOf('\0') >= 0
        || token.isBlank()
        || (shard + '\0' + token).getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 4096)
      throw new IllegalArgumentException("Invalid shard credentials");
    this.loops = new NioEventLoopGroup(1);
    this.host = host;
    this.port = port;
    this.shard = shard;
    this.token = token;
    this.settings = settings;
    this.listener = listener;
    delay = settings.reconnectInitial().toMillis();
  }

  public synchronized CompletionStage<Void> start() {
    if (closed) return CompletableFuture.failedFuture(new IllegalStateException("Client closed"));
    if (!started) {
      started = true;
      LOG.info("Starting gateway connection to {}:{} for shard {}", host, port, shard);
      loops.next().execute(this::connect);
    }
    return ready;
  }

  private void connect() {
    if (closed) return;
    new Bootstrap()
        .group(loops)
        .channel(NioSocketChannel.class)
        .option(
            ChannelOption.CONNECT_TIMEOUT_MILLIS,
            (int) Math.min(Integer.MAX_VALUE, settings.handshakeTimeout().toMillis()))
        .option(ChannelOption.TCP_NODELAY, true)
        .handler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel ch) {
                TunnelPipeline.install(
                    ch,
                    settings,
                    new TunnelClientHandler(
                        GatewayTunnelClient.this, shard, token, listener, settings));
              }
            })
        .connect(host, port)
        .addListener(
            (ChannelFuture f) -> {
              if (f.isSuccess()) {
                channel = f.channel();
                if (closed) channel.close();
              } else {
                connectionError("TCP connection failed", f.cause());
                scheduleReconnect();
              }
            });
  }

  void registered() {
    registered = true;
    delay = settings.reconnectInitial().toMillis();
    LOG.info("Gateway connection established successfully with {}:{}; shard {} registered", host, port, shard);
    ready.complete(null);
  }

  void disconnected() {
    if (!closed) {
      LOG.warn("Gateway connection to {}:{} closed for shard {} ({})", host, port, shard,
          registered ? "registered tunnel lost; sessions will be closed" : "registration was not completed");
    }
    registered = false;
    scheduleReconnect();
  }

  private void scheduleReconnect() {
    if (closed || retry != null && !retry.isDone()) return;
    LOG.info("Retrying gateway connection to {}:{} for shard {} in {} ms", host, port, shard, delay);
    retry =
        loops
            .next()
            .schedule(
                () -> {
                  retry = null;
                  connect();
                },
                delay,
                TimeUnit.MILLISECONDS);
    delay =
        Math.min(
            settings.reconnectMax().toMillis(),
            delay > Long.MAX_VALUE / 2 ? Long.MAX_VALUE : delay * 2);
  }

  public boolean isRegistered() {
    return registered;
  }

  void connectionError(String reason, Throwable cause) {
    if (closed) return;
    if (cause == null) {
      LOG.warn("Gateway connection error at {}:{} for shard {}: {}", host, port, shard, reason);
    } else {
      LOG.warn("Gateway connection error at {}:{} for shard {}: {}", host, port, shard, reason, cause);
    }
  }

  public CompletionStage<Void> termination() {
    return stopped;
  }

  @Override
  public synchronized void close() {
    if (closed) return;
    closed = true;
    registered = false;
    ready.completeExceptionally(new CancellationException("Client stopped"));
    loops
        .next()
        .execute(
            () -> {
              if (retry != null) retry.cancel(false);
              if (channel != null) channel.close();
              loops
                  .shutdownGracefully(0, 5, TimeUnit.SECONDS)
                  .addListener(
                      f -> {
                        if (f.isSuccess()) stopped.complete(null);
                        else stopped.completeExceptionally(f.cause());
                      });
            });
  }
}
