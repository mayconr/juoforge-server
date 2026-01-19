package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.PlayerDetails;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.game.GameSession;
import com.github.mayconr.juoserver.game.world.WorldService;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileStorage;
import com.github.mayconr.juoserver.network.packet.ClientVersion;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class CreateCharacterHandler extends SessionChannelInboundHandler<CreateCharacter> {

    private final WorldService worldService;
    private final GameSession gameSession;
    private final MobileStorage mobileStorage;

    @Override
    protected void channelRead0(SessionOutbound outbound, CreateCharacter msg) {
        final var account = outbound.attr().get(AttributeKeys.ACCOUNT_KEY);
        final var details = new PlayerDetails(account, "pass", msg.getCharacterName());

        mobileStorage.mobileExists(msg.getCharacterName())
            .thenAccept(exists->{
                if (exists) {
                    outbound.writeAndFlush(new LoginReject(LoginReject.Reason.CHAR_ALREADY_EXIST));
                } else {
                    handlePlayerCreation(details, outbound);
                }
            }).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to create mobile", throwable);
                    outbound.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
                }
            });
    }

    private void handlePlayerCreation(PlayerDetails details, SessionOutbound outbound) {
        worldService.createPlayer(details)
            .thenAccept(player -> {
                final var session = gameSession.createPlayerSession(player, outbound);
                outbound.attr().set(AttributeKeys.PLAYER_SESSION_KEY, session);
                outbound.writeAndFlush(new ClientVersion());
            })
            .whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    outbound.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
                    log.error("Unable to create character [{}]", details.name(), throwable);
                }
            });
    }
}
