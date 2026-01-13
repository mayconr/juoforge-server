package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.model.UOPlayer;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.packet.ClientVersion;
import com.github.mayconr.juoserver.game.packet.LoginCharacter;
import com.github.mayconr.juoserver.game.packet.LoginReject;
import com.github.mayconr.juoserver.game.storage.WorldService;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class LoginCharacterHandler extends SimpleChannelInboundHandler<LoginCharacter> {

    private final GameSession gameSession;
    private final WorldService worldService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginCharacter msg) throws Exception {
        final var channel = ctx.channel();

        final var slots = channel.attr(AttributeKeys.CHARACTERS_SLOT).getAndSet(null);
        final var selectedMobile = slots.get(msg.getSelectedSlot());
        if (selectedMobile.name().equals(msg.getCharacterName())) {
            worldService
                    .findMobileBySerialId(selectedMobile.serialId())
                    .whenComplete(
                            (mob, ex) -> {
                                if (ex != null) {
                                    log.error(
                                            "Unable do load mobile info for [{}]",
                                            msg.getCharacterName(),
                                            ex);
                                    return;
                                }
                                if (mob.isEmpty()) {
                                    log.warn(
                                            "Mobile not found [name={}, slot={}]",
                                            msg.getCharacterName(),
                                            msg.getSelectedSlot());
                                    return;
                                }
                                final var mobile = mob.get();

                                if (mobile instanceof UOPlayer player) {
                                    channel.attr(AttributeKeys.PLAYER_SESSION)
                                            .set(gameSession.createPlayerSession(player, ctx));
                                    ctx.writeAndFlush(new ClientVersion());
                                } else {
                                    log.error("Mobile [{}] is not a player", mobile.getName());
                                }
                            });
        } else {
            ctx.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
        }
    }
}
