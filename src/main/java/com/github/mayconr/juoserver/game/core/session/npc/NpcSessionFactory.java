package com.github.mayconr.juoserver.game.core.session.npc;

import com.github.mayconr.juoserver.game.core.ai.NpcAiRegistry;
import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.core.model.UONpc;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;

import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NpcSessionFactory {

    private final EventBus eventBus;
    private final ChannelGroup channelGroup;
    private final GameLoop gameLoop;
    private final NpcAiRegistry aiRegistry;

    public NpcSession create(GameSession gameSession, UONpc npc) {
        try {
            final var npcAI = aiRegistry.create(npc.getAi());
            final var movementService = new MovementService(channelGroup, npc);
            final var session =
                    new DefaultNpcSession(
                            gameSession, npc, channelGroup, eventBus, npcAI, movementService);
            gameLoop.addTasks(new NpcVitalsTask(session), npcAI);
            return session;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create AI for [" + npc.getAi() + "]", e);
        }
    }
}
