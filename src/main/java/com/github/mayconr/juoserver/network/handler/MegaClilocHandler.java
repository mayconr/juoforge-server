package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.MegaCliloc;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class MegaClilocHandler extends PlayerSessionChannelInboundHandler<MegaCliloc> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, MegaCliloc msg) {
        session.showMegaCliloc(msg.getSerialList());
    }
}
