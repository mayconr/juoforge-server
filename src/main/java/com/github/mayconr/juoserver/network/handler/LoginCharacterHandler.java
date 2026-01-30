package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.LoginCharacter;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class LoginCharacterHandler extends SessionChannelInboundHandler<LoginCharacter> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(SessionOutbound outbound, LoginCharacter msg) {
        final var slots = outbound.attr().remove(AttributeKeys.SESSION_CREATION_CONTEXT).mobiles();

        final var selectedMobile = slots.get(msg.getSelectedSlot());
        if (selectedMobile.name().equals(msg.getCharacterName())) {
            world.loadMobile(selectedMobile.serialId())
                .thenAccept(mobile->{
                    if (mobile instanceof UOPlayer player) {
                        world.loginExistingPlayer(player, outbound)
                            .whenComplete((unused, throwable) -> {
                                if (throwable != null) {
                                    log.error("Unable to login existing player", throwable);
                                    return;
                                }

                                log.info("Player [{}] logged in!", player.getName());
                            });
                    } else {
                        log.error("Mobile [{}] is not a player", mobile.getName());
                        outbound.write(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
                    }
                });
        } else {
            outbound.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
        }
    }

}
