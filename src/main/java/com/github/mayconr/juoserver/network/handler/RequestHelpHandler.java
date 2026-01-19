package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.RequestHelp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class RequestHelpHandler extends PlayerSessionChannelInboundHandler<RequestHelp> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, RequestHelp msg) {
        System.out.println(session.getPlayer().getName() + "asked for help " + msg);
    }
}
