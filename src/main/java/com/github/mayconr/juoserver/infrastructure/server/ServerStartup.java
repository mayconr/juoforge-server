package com.github.mayconr.juoserver.infrastructure.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import com.github.mayconr.juoserver.network.NetworkServer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
public class ServerStartup implements NetworkServer {

    private final ServerBootstrap serverBootstrap;
    private final NioEventLoopGroup parentNioEventLoopGroup;
    private final NioEventLoopGroup childNioEventLoopGroup;
    private final String host;
    private final int port;
    private final CompletableFuture<Void> ready = new CompletableFuture<>();
    private Channel channel;
    private boolean started;
    private boolean closed;

    public ServerStartup(ServerBootstrap bootstrap, NioEventLoopGroup parent, NioEventLoopGroup child) {
        this(bootstrap, parent, child, "0.0.0.0", 2593);
    }

    public ServerStartup(ServerBootstrap bootstrap, NioEventLoopGroup parent, NioEventLoopGroup child, String host, int port) {
        this.serverBootstrap = bootstrap;
        this.parentNioEventLoopGroup = parent;
        this.childNioEventLoopGroup = child;
        this.host = host;
        this.port = port;
    }

    @Override
    public CompletionStage<Void> start() {
        bindAsync(port);
        return ready;
    }

    public synchronized void bindAsync(int port) {
        if (closed) throw new IllegalStateException("Server is closed");
        if (started) return;
        started = true;
        log.info("Server initializing...");

        serverBootstrap.bind(host, port).addListener((ChannelFuture future) -> {
            if (!future.isSuccess()) {
                log.error("Failed to initialize server on port {}", port, future.cause());
                ready.completeExceptionally(future.cause());
                shutdown();
                return;
            }

            log.info("Server initialized on port {}!", port);
            synchronized (this) {
                channel = future.channel();
                if (closed) channel.close();
                else ready.complete(null);
            }

            future.channel()
                    .closeFuture()
                    .addListener(closeFuture -> {
                        if (closeFuture.isSuccess()) {
                            log.info("Server stopped!");
                        } else {
                            log.error("Server stopped with error", closeFuture.cause());
                        }
                        shutdown();
                    });
        });
    }

    private void shutdown() {
        log.info("Server shutting down gracefully...");
        parentNioEventLoopGroup.shutdownGracefully();
        childNioEventLoopGroup.shutdownGracefully();
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        ready.completeExceptionally(new CancellationException("Server stopped"));
        if (channel != null) channel.close();
        shutdown();
    }
}
