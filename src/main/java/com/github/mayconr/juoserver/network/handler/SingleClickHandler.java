package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class SingleClickHandler extends PlayerSessionChannelInboundHandler<SingleClickRequest> {

    private final WorldInternal  world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, SingleClickRequest msg) {
        world.singleClick((UOPlayer) session.getPlayer(), msg);
    }
}
