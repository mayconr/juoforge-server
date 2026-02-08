package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class MoveRequestHandler extends PlayerSessionChannelInboundHandler<MoveRequest> {

    private final WorldInternal worldInternal;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, MoveRequest msg) {
        worldInternal.move((UOPlayer) session.getPlayer(), msg);
    }
}
