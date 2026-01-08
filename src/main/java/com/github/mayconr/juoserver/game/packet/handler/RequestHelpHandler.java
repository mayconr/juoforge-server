package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.RequestHelp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class RequestHelpHandler extends PlayerSessionChannelInboundHandler<RequestHelp> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, RequestHelp msg) {
        System.out.println(session.getPlayer().getName() + "asked for help " + msg);
    }
}
