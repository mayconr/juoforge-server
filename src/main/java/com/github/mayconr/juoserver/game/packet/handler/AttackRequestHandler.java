package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.AttackRequest;

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
