package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class CreateCharacterHandler extends SessionChannelInboundHandler<CreateCharacter> {

    private final RealmStorage realmStorage;
    private final WorldSession worldSession;
    private final MobileStorage mobileStorage;

    @Override
    protected void channelRead0(SessionOutbound outbound, CreateCharacter character) {
        final var account = outbound.attr().get(AttributeKeys.ACCOUNT_KEY);
        worldSession.createAndLoginPlayer(account, character, outbound);
    }

    /*private void handlePlayerCreation(PlayerDetails details, SessionOutbound outbound) {
        realmStorage.createPlayer(details)
            .thenAccept(player -> {
                final var session = worldSession.createPlayerSession(player, outbound);
                outbound.attr().set(AttributeKeys.PLAYER_SESSION_KEY, session);
                outbound.writeAndFlush(new ClientVersion());
            })
            .whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    outbound.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
                    log.error("Unable to create character [{}]", details.name(), throwable);
                }
            });
    }*/
}
