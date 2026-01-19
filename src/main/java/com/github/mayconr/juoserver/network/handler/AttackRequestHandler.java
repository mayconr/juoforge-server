package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.AttackRequest;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class AttackRequestHandler extends PlayerSessionChannelInboundHandler<AttackRequest> {
    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, AttackRequest msg) {
        session.attack(msg.getOpponentSerialId());
    }
}
