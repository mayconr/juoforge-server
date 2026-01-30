package com.github.mayconr.juoserver.game.session.npc;

import com.github.mayconr.juoserver.game.ai.NpcAiRegistry;
import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;

import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NpcSessionFactory {

    private final EventBus eventBus;
    private final ChannelGroup channelGroup;
    private final GameLoop gameLoop;
    private final NpcAiRegistry aiRegistry;

    public NpcSession create(WorldInternal worldInternal, UONpc npc) {
        try {
            final var npcAI = aiRegistry.create(npc.getAi());
            final var movementService = new MovementService(channelGroup, npc);
            final var session =
                    new DefaultNpcSession(
                            worldInternal, npc, channelGroup, eventBus, npcAI, movementService);
            gameLoop.addTasks(new NpcVitalsTask(session), npcAI);
            return session;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create AI for [" + npc.getAi() + "]", e);
        }
    }
}
