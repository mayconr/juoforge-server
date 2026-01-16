package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.SessionOutbound;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.ClientVersion;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ClientVersionHandler extends PlayerSessionChannelInboundHandler<ClientVersion> {

    private final GameSession gameSession;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, ClientVersion msg) {
        session.initialize(gameSession, msg.getClientVersion());
    }

}
