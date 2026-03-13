package com.github.mayconr.juoserver.infrastructure.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void bind(int port) {
        try {
            LOGGER.info("Server initializing...");
            var future = serverBootstrap.bind(port);
            LOGGER.info("Server initialized on port "+port+"!");
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
