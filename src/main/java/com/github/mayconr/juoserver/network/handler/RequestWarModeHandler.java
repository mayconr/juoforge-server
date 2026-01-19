package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.RequestWarMode;

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
