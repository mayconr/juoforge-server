package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.ClientVersion;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ClientVersionHandler extends PlayerSessionChannelInboundHandler<ClientVersion> {

    private final GameSession gameSession;

    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, ClientVersion msg) {
        session.initialize(gameSession, msg.getClientVersion());
    }
}
