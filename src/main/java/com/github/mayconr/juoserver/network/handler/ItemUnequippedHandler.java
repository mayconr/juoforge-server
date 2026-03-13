package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class ItemUnequippedHandler extends PlayerSessionChannelInboundHandler<UnequipItem> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, UnequipItem msg) {
        world.unequipItem(session.getPlayer(), msg);
    }
}
