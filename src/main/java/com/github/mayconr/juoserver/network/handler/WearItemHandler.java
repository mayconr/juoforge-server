package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class WearItemHandler extends PlayerSessionChannelInboundHandler<EquipItemRequest> {
    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, EquipItemRequest msg) {
        session.equipItem(msg);
    }
}
