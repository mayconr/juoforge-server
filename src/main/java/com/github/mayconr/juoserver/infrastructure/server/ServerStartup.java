package com.github.mayconr.juoserver.infrastructure.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@RequiredArgsConstructor
public class ServerStartup {

    private final ServerBootstrap serverBootstrap;
    private final NioEventLoopGroup parentNioEventLoopGroup;
    private final NioEventLoopGroup childNioEventLoopGroup;

    public void bindAsync(int port) {
        log.info("Server initializing...");

        serverBootstrap.bind(port).addListener((ChannelFuture future) -> {
            if (!future.isSuccess()) {
                log.error("Failed to initialize server on port {}", port, future.cause());
                shutdown();
                return;
            }

            log.info("Server initialized on port {}!", port);

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
}
