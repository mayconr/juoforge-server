package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.Target;

import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class TargetHandler extends PlayerSessionChannelInboundHandler<Target> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, Target msg) {
        world.resolveTarget((UOPlayer) session.getPlayer(), msg);
    }
}
