package com.github.mayconr.juoserver.game.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;

@ChannelHandler.Sharable
public class ClientConnectedHandlerAdapter extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ClientConnectedHandlerAdapter.class);
    private final ChannelGroup channelGroup;

    public ClientConnectedHandlerAdapter(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channelGroup.add(ctx.channel());
        final var remoteAddress = ctx.channel().remoteAddress();
        LOGGER.info("New connection from: " + remoteAddress);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        channelGroup.remove(ctx.channel());
        final var remoteAddress = ctx.channel().remoteAddress();
        LOGGER.info("Connection closed: " + remoteAddress);
    }
}
