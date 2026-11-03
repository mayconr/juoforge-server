package com.github.mayconr.juoserver.infrastructure.gateway;

import com.github.mayconr.juoserver.NetworkSettings;
import com.github.mayconr.juoserver.infrastructure.gateway.client.*;
import com.github.mayconr.juoserver.infrastructure.gateway.protocol.TunnelSettings;
import com.github.mayconr.juoserver.network.NetworkServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.local.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Adapts tunnel sessions to the existing UO pipeline using in-memory Netty channels.
 * No TCP game listener is opened in this mode. Gateway types never enter gameplay code.
 */
public final class GatewayNetworkServer implements NetworkServer, TunnelSessionListener {
    private final DefaultEventLoopGroup localLoops = new DefaultEventLoopGroup(1);
    private final LocalAddress address = new LocalAddress("juoforge-" + UUID.randomUUID());
    private final Map<Long, Bridge> sessions = new ConcurrentHashMap<>();
    private final TunnelSettings limits = TunnelSettings.defaults();
    private final GatewayTunnelClient client;
    private final ChannelInitializer<Channel> gamePipeline;
    private final CompletableFuture<Void> ready = new CompletableFuture<>();
    private Channel listener;
    private boolean started;
    private boolean closed;

    public GatewayNetworkServer(NetworkSettings settings, ChannelInitializer<Channel> gamePipeline) {
        this.gamePipeline = gamePipeline;
        this.client = new GatewayTunnelClient(settings.host(), settings.port(), settings.shard(),
                settings.token(), limits, this);
    }

    @Override
    public synchronized CompletionStage<Void> start() {
        if (closed) return CompletableFuture.failedFuture(new IllegalStateException("Server closed"));
        if (started) return ready;
        started = true;
        new ServerBootstrap().group(localLoops).channel(LocalServerChannel.class)
                .childHandler(gamePipeline).bind(address).addListener((ChannelFuture bound) -> {
                    synchronized (this) {
                        if (!bound.isSuccess()) {
                            ready.completeExceptionally(bound.cause());
                            close();
                            return;
                        }
                        listener = bound.channel();
                        if (closed) {
                            listener.close();
                            return;
                        }
                        client.start().whenComplete((ignored, error) -> {
                            if (error == null) ready.complete(null);
                            else ready.completeExceptionally(error);
                        });
                    }
                });
        return ready;
    }

    @Override
    public synchronized boolean onOpen(TunnelSession session) {
        if (closed) return false;
        Bridge bridge = new Bridge();
        sessions.put(session.id(), bridge);
        bridge.connection = new Bootstrap().group(localLoops).channel(LocalChannel.class)
                .handler(new ChannelInitializer<LocalChannel>() {
                    @Override
                    protected void initChannel(LocalChannel channel) {
                        channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf bytes) {
                                byte[] data = new byte[bytes.readableBytes()];
                                bytes.readBytes(data);
                                session.send(data).whenComplete((ignored, error) -> {
                                    if (error != null) ctx.close();
                                });
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) {
                                session.close();
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable error) {
                                ctx.close();
                            }
                        });
                    }
                }).connect(address);
        bridge.connection.addListener((ChannelFuture connected) -> {
            if (!connected.isSuccess()) session.close();
        });
        return true;
    }

    @Override
    public void onData(TunnelSession session, byte[] data) {
        Bridge bridge = sessions.get(session.id());
        if (bridge == null) return;
        if (bridge.pending.addAndGet(data.length) > limits.bufferBytes()) {
            bridge.pending.addAndGet(-data.length);
            session.close();
            return;
        }
        bridge.connection.addListener((ChannelFuture connected) -> {
            if (!connected.isSuccess()) {
                bridge.pending.addAndGet(-data.length);
                session.close();
                return;
            }
            connected.channel().writeAndFlush(Unpooled.wrappedBuffer(data)).addListener(written -> {
                bridge.pending.addAndGet(-data.length);
                if (!written.isSuccess()) session.close();
            });
        });
    }

    @Override
    public void onClose(TunnelSession session) {
        Bridge bridge = sessions.remove(session.id());
        if (bridge != null) bridge.connection.channel().close();
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        ready.completeExceptionally(new CancellationException("Server stopped"));
        client.close();
        if (listener != null) listener.close();
        sessions.values().forEach(bridge -> bridge.connection.channel().close());
        localLoops.shutdownGracefully(0, 5, TimeUnit.SECONDS);
    }

    private static final class Bridge {
        private final AtomicInteger pending = new AtomicInteger();
        private ChannelFuture connection;
    }
}
