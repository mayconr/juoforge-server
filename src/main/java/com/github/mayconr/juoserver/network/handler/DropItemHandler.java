package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.DropItem;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@ChannelHandler.Sharable
public class DropItemHandler extends PlayerSessionChannelInboundHandler<DropItem> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, DropItem msg) {
        if (msg.isContainerDrop()) {
            world.dropItemInContainer((UOPlayer) session.getPlayer(), msg);
        } else {
            world.dropItemOnTheGround((UOPlayer) session.getPlayer(), msg);
        }
    }
}
