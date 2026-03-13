package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.LoginCharacter;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class LoginCharacterHandler extends PlayerSessionChannelInboundHandler<LoginCharacter> {

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, LoginCharacter msg) {

        var selected = session.selectCharacter(msg.getSelectedSlot());
        if (log.isDebugEnabled()) {
            log.debug("Selected character: {}", selected);
        }

        session.enteringWorld()
                .thenAccept(player -> session.activate());
    }
}
