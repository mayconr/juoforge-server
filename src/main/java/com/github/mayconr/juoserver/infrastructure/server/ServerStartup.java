package com.github.mayconr.juoserver.infrastructure.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import jakarta.annotation.PostConstruct;

public class ServerStartup {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStartup.class);
    private final ServerBootstrap serverBootstrap;
    private final NioEventLoopGroup parentNioEventLoopGroup;
    private final NioEventLoopGroup childNioEventLoopGroup;

    public ServerStartup(
            ServerBootstrap serverBootstrap,
            NioEventLoopGroup parentNioEventLoopGroup,
            NioEventLoopGroup childNioEventLoopGroup) {
        this.serverBootstrap = serverBootstrap;
        this.parentNioEventLoopGroup = parentNioEventLoopGroup;
        this.childNioEventLoopGroup = childNioEventLoopGroup;
    }

    @PostConstruct
    public void initialize() throws InterruptedException {
        try {
            LOGGER.info("Server initializing...");
            var future = serverBootstrap.bind(9000);
            LOGGER.info("Server initialized on port 9000!");
            future.channel()
                    .closeFuture()
                    .addListener(
                            f -> {
                                LOGGER.info("Server stopped!");
                            });
        } finally {
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread(
                                    () -> {
                                        LOGGER.info("Server shutting down gracefully...");
                                        parentNioEventLoopGroup.shutdownGracefully();
                                        childNioEventLoopGroup.shutdownGracefully();
                                    }));
        }
    }
}
