package com.github.mayconr.juoserver.infrastructure.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UOChannelInitializer extends ChannelInitializer<Channel> {

    private final ClientConnectedHandlerAdapter clientConnectedHandler;
    private final List<SimpleChannelInboundHandler<?>> packetHandlers;

    @Override
    protected void initChannel(Channel ch) {
        ch.pipeline().addLast(clientConnectedHandler);
        ch.pipeline().addLast(new UOProtocolDecoder());
        ch.pipeline().addLast(new UOProtocolEncoder());
        for (SimpleChannelInboundHandler<?> handler : packetHandlers) {
            ch.pipeline().addLast(handler);
        }
        ch.pipeline().addLast(new ErrorHandler());
    }
}
