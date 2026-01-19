package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.game.GameSession;
import com.github.mayconr.juoserver.network.packet.ClientVersion;
import com.github.mayconr.juoserver.network.packet.LoginCharacter;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import com.github.mayconr.juoserver.infrastructure.server.Future;
import com.github.mayconr.juoserver.game.world.WorldService;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class LoginCharacterHandler extends SessionChannelInboundHandler<LoginCharacter> {

    private final GameSession gameSession;
    private final WorldService worldService;

    @Override
    protected void channelRead0(SessionOutbound outbound, LoginCharacter msg) {
        final var slots = outbound.attr().remove(AttributeKeys.CHARACTERS_SLOT_KEY);
        final var selectedMobile = slots.get(msg.getSelectedSlot());
        if (selectedMobile.name().equals(msg.getCharacterName())) {
            Future.fire(worldService.findMobileBySerialId(selectedMobile.serialId())
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
                        outbound.attr().set(AttributeKeys.PLAYER_SESSION_KEY, gameSession.createPlayerSession(player, outbound.getCtx(), outbound));
                        outbound.writeAndFlush(new ClientVersion());
                        System.out.println("enviado version");
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
