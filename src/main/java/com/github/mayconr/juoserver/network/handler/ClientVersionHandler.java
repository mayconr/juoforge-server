package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.ClientVersion;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ClientVersionHandler extends PlayerSessionChannelInboundHandler<ClientVersion> {

    private final WorldSession worldSession;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, ClientVersion msg) {
        session.initialize(worldSession, msg.getClientVersion());
    }

}
