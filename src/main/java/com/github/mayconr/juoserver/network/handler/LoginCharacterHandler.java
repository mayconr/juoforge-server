package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.SessionFanout;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.player.SessionManager;
import com.github.mayconr.juoserver.game.world.WorldInternal;
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
    private final PlayerSessionFactory playerSessionFactory;
    private final SessionManager sessionManager;
    private final SessionFanout fanout;

    @Override
    protected void channelRead0(SessionOutbound outbound, LoginCharacter msg) {

        final var context = outbound.attr()
                .remove(AttributeKeys.SESSION_CREATION_CONTEXT);

        final var selectedMobile =
                context.mobiles().get(msg.getSelectedSlot());

        if (!selectedMobile.name().equals(msg.getCharacterName())) {
            reject(outbound);
            return;
        }

        world.loadMobile(selectedMobile.serialId())
                .thenAccept(mobile -> handleLoadedMobile(mobile, outbound));
    }

    private void handleLoadedMobile(
            UOMobile mobile,
            SessionOutbound outbound) {

        if (!(mobile instanceof UOPlayer player)) {
            log.error("Mobile [{}] is not a player", mobile.getName());
            reject(outbound);
            return;
        }

        createSessionAndLogin(player, outbound);
    }

    private void createSessionAndLogin(UOPlayer player, SessionOutbound outbound) {

        final var session = playerSessionFactory.createPlayerSession(player, outbound, fanout);

        sessionManager.register(session);
    }

    private void reject(SessionOutbound outbound) {
        outbound.writeAndFlush(
                new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR)
        );
    }
}
