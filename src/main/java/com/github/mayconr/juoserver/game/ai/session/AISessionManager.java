package com.github.mayconr.juoserver.game.ai.session;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.ai.NpcAiRegistry;
import com.github.mayconr.juoserver.game.ai.BehaviorProfileRegistry;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class AISessionManager {

    private final Map<Integer, AISession> sessions = new ConcurrentHashMap<>();
    private final EventBus eventBus;
    private final BehaviorProfileRegistry profileRegistry;
    private final NpcAiRegistry aiFactory;

    public AISession attach(UONpc npc) {
        final var profile = profileRegistry.get(npc.getBehavior().profile());
        if (profile == null) {
            throw new IllegalStateException("No profile found for " + npc.getBehavior().profile());
        }

        final var ai = aiFactory.get(npc.getBehavior().ai());
        if (ai == null) {
            throw new IllegalStateException("No ai found for " + npc.getBehavior().ai());
        }

        final var session = new AISessionImpl(npc, eventBus, profile, ai);
        sessions.put(npc.getSerialId(), session);
        log.info("Attaching AI [{}] for NPC [{}]", npc.getBehavior().ai(), npc.getName());
        return session;
    }

    public void detach(UONpc npc) {
        sessions.compute(npc.getSerialId(), (key, session)->{
            if (session != null) {
                session.kill();
                log.info("Detaching AI [{}] for NPC [{}]", npc.getBehavior().ai(), npc.getName());
            }
           return null;
        });
    }

    public void update(double delta) {
        for (AISession session : sessions.values()) {
            session.think(delta);
        }
    }
}
