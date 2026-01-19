package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.Target;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class TargetHandler extends PlayerSessionChannelInboundHandler<Target> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, Target msg) {
        session.handleTarget(msg);
    }
}
