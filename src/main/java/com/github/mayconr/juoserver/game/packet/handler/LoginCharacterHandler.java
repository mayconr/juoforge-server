package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.packet.ClientVersion;
import com.github.mayconr.juoserver.game.packet.LoginCharacter;
import com.github.mayconr.juoserver.game.packet.LoginReject;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class LoginCharacterHandler extends SimpleChannelInboundHandler<LoginCharacter> {

    private final GameSession gameSession;

    public LoginCharacterHandler(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginCharacter msg) throws Exception {
        final var channel = ctx.channel();

        final var slots = channel.attr(AttributeKeys.CHARACTERS_SLOT).getAndSet(null);
        final var mobile = slots.get(msg.getSelectedSlot());
        if (mobile.getName().equals(msg.getCharacterName())) {
            channel.attr(AttributeKeys.PLAYER_SESSION)
                    .set(gameSession.createPlayerSession(mobile, ctx));
            ctx.writeAndFlush(new ClientVersion());
        } else {
            ctx.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
        }
    }
}
