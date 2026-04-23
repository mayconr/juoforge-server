package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class CreatePlayerHandler extends PlayerSessionChannelInboundHandler<CreateCharacter> {

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, CreateCharacter msg) {
        session.createCharacter(msg)
                .thenCompose(player->session.enteringWorld())
                .thenAccept(player->session.activate())
                .whenComplete((player, throwable) -> {
                    if (throwable != null) {
                        log.error("Error to create character [{}]", msg, throwable);
                    }
                });
    }

}
