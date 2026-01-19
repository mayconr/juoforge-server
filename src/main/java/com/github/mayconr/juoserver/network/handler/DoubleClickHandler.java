package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.DoubleClick;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class DoubleClickHandler extends PlayerSessionChannelInboundHandler<DoubleClick> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, DoubleClick msg) {
        session.doubleClick(msg);
    }
}
