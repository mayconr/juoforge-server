package com.github.mayconr.juoserver.infrastructure.server;

import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;

public class UOChannelInitializer extends ChannelInitializer<Channel> {

    private final ClientConnectedHandlerAdapter clientConnectedHandler;
    private final List<SimpleChannelInboundHandler<?>> packetHandlers;

    public UOChannelInitializer(
            ClientConnectedHandlerAdapter clientConnectedHandler,
            List<SimpleChannelInboundHandler<?>> packetHandlers) {
        this.clientConnectedHandler = clientConnectedHandler;
        this.packetHandlers = packetHandlers;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(clientConnectedHandler);
        ch.pipeline().addLast(new UOProtocolDecoder());
        ch.pipeline().addLast(new UOProtocolEncoder());
        for (SimpleChannelInboundHandler<?> handler : packetHandlers) {
            ch.pipeline().addLast(handler);
        }
        ch.pipeline().addLast(new ErrorHandler());
    }
}
