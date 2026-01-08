package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.DropItem;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class DropItemHandler extends PlayerSessionChannelInboundHandler<DropItem> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, DropItem msg) {
        if (msg.isContainerDrop()) {
            session.dropItemInContainer(msg);
        } else {
            session.dropItemOnTheGround(msg);
        }
    }
}
