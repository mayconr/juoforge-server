package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.network.packet.ClientVersion;
import com.github.mayconr.juoserver.network.packet.LoginCharacter;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import com.github.mayconr.juoserver.infrastructure.server.Future;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class LoginCharacterHandler extends SessionChannelInboundHandler<LoginCharacter> {

    private final WorldSession worldSession;
    private final RealmStorage realmStorage;

    @Override
    protected void channelRead0(SessionOutbound outbound, LoginCharacter msg) {
        final var slots = outbound.attr().remove(AttributeKeys.CHARACTERS_SLOT_KEY);
        final var selectedMobile = slots.get(msg.getSelectedSlot());
        if (selectedMobile.name().equals(msg.getCharacterName())) {
            Future.fire(realmStorage.findMobileBySerialId(selectedMobile.serialId())
                .thenAccept(opt->{
                    if (opt.isEmpty()) {
                        log.warn(
                                "Mobile not found [name={}, slot={}]",
                                msg.getCharacterName(),
                                msg.getSelectedSlot());
                        outbound.writeAndFlush(new LoginReject(LoginReject.Reason.CHAR_DOES_NOT_EXIST));
                        return;
                    }

                    final var mobile = opt.get();
                    if (mobile instanceof UOPlayer player) {
                        worldSession.loginExistingPlayer(player, outbound)
                                .thenAccept(session->{
                                    log.info("Player [{}] logged in!", player.getName());
                                });
                    } else {
                        log.error("Mobile [{}] is not a player", mobile.getName());
                    }
                })
            );
        } else {
            outbound.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
        }
    }

}
