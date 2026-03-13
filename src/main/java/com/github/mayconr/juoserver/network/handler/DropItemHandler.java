package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.DropItem;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@ChannelHandler.Sharable
public class DropItemHandler extends PlayerSessionChannelInboundHandler<DropItem> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, DropItem msg) {
        if (msg.isContainerDrop()) {
            world.dropItemInContainer(session.getPlayer(), msg);
        } else {
            world.dropItemOnTheGround(session.getPlayer(), msg);
        }
    }
}
