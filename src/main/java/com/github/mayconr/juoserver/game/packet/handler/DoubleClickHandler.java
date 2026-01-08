package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.DoubleClick;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class DoubleClickHandler extends PlayerSessionChannelInboundHandler<DoubleClick> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, DoubleClick msg) {
        session.doubleClick(msg);
    }
}
