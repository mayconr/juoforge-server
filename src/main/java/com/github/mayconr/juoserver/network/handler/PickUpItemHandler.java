package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.PickUpItem;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class PickUpItemHandler extends PlayerSessionChannelInboundHandler<PickUpItem> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, PickUpItem msg) {
        session.pickUpItem(msg);
    }
}
