package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.MegaCliloc;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class MegaClilocHandler extends PlayerSessionChannelInboundHandler<MegaCliloc> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, MegaCliloc msg) {
        session.showMegaCliloc(msg.getSerialList());
    }
}
