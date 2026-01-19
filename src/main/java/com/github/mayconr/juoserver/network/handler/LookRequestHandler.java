package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.LookRequest;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class LookRequestHandler extends PlayerSessionChannelInboundHandler<LookRequest> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, LookRequest msg) {
        System.out.println("recebeu look " + msg.getSerialId());
    }
}
