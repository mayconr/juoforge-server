package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.ClientVersion;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ClientVersionHandler extends PlayerSessionChannelInboundHandler<ClientVersion> {

    private final WorldInternal worldInternal;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, ClientVersion msg) {
        session.initialize(worldInternal, msg.getClientVersion());
    }

}
