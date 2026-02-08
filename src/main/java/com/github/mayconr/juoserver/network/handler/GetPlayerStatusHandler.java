package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.GetPlayerStatus;
import com.github.mayconr.juoserver.network.packet.StatusBarInfo;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class GetPlayerStatusHandler extends PlayerSessionChannelInboundHandler<GetPlayerStatus> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, GetPlayerStatus msg) {
        world.playerStatusRequested((UOPlayer) session.getPlayer(), msg);
    }
}
