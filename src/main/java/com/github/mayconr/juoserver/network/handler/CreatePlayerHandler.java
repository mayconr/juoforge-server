package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.player.SessionFanout;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.player.SessionRegistry;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class CreatePlayerHandler extends SessionChannelInboundHandler<CreateCharacter> {

    private final WorldInternal world;
    private final PlayerSessionFactory playerSessionFactory;
    private final SessionFanout fanout;
    private final SessionRegistry sessionRegistry;

    @Override
    protected void channelRead0(SessionOutbound outbound, CreateCharacter character) {
        final var cities = outbound.attr()
                .remove(AttributeKeys.SESSION_CREATION_CONTEXT)
                .cities();
        final var account = outbound.attr().get(AttributeKeys.ACCOUNT_KEY);

        world.createPlayer(character, cities, account)
            .thenAccept(player -> {
                var session = playerSessionFactory.createPlayerSession(player, outbound, fanout);
                sessionRegistry.register(session);
            });
    }

}
