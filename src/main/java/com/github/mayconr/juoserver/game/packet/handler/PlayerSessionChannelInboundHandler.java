package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class PlayerSessionChannelInboundHandler<T> extends SimpleChannelInboundHandler<T> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        channelRead0(ctx.channel().attr(AttributeKeys.PLAYER_SESSION).get(), ctx, msg);
    }

    protected abstract void channelRead0(PlayerSession session, ChannelHandlerContext ctx, T msg);
}
