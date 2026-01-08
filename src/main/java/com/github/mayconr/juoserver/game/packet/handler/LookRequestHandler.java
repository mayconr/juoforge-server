package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.LookRequest;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class LookRequestHandler extends PlayerSessionChannelInboundHandler<LookRequest> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, LookRequest msg) {
        System.out.println("recebeu look " + msg.getSerialId());
    }
}
