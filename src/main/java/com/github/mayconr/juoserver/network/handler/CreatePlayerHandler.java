package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class CreatePlayerHandler extends SessionChannelInboundHandler<CreateCharacter> {

    private final WorldInternal worldInternal;

    @Override
    protected void channelRead0(SessionOutbound outbound, CreateCharacter character) {
        worldInternal.createAndLoginPlayer(character, outbound)
                .whenComplete((session, throwable) -> {
                    if (throwable != null) {
                        log.error("Unable to create player session", throwable);
                    }
                });
    }

}
