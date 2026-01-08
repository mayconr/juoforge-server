package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.RequestWarMode;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class RequestWarModeHandler extends PlayerSessionChannelInboundHandler<RequestWarMode> {
    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, RequestWarMode msg) {
        session.handleWarMode(msg.getType());
    }
}
