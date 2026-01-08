package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.PickUpItem;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class PickUpItemHandler extends PlayerSessionChannelInboundHandler<PickUpItem> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, PickUpItem msg) {
        session.pickUpItem(msg);
    }
}
