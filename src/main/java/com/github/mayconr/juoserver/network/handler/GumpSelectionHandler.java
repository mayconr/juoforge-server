package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class GumpSelectionHandler extends PlayerSessionChannelInboundHandler<GumpSelection> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, GumpSelection msg) {
        world.gumpResponse(session.getPlayer(), msg);
    }
}
