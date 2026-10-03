package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.engine.AIEngine;
import com.github.mayconr.juoserver.game.ai.session.AISession;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class AIModuleImpl implements AIModule {
    private final AIEngine engine;
    private final EventBus eventBus;
    private final Map<Integer, AISession<?>> sessions = new ConcurrentHashMap<>();
    private final Map<Integer, EventHandler<MobileSpeech>> speechListeners = new ConcurrentHashMap<>();

    @Override
    public void initialize(ModuleContext context) {
        engine.initialize(context.flows());
        log.info("AI Module initialized");
    }

    @Override
    public void update(double delta) {
        engine.update(delta);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AIFlowContext> AISession<T> attach(UONpc npc) {
        var existing = sessions.get(npc.getSerialId());
        if (existing != null) return (AISession<T>) existing;
        var session = engine.attach(npc);

        // Register default events
        EventHandler<MobileSpeech> listener = session::onSpeech;
        eventBus.register(MobileSpeech.class, listener);
        speechListeners.put(npc.getSerialId(), listener);

        sessions.put(npc.getSerialId(), session);

        log.info("Session attached to AI Module");

        return (AISession<T>) session;
    }

    @Override
    public void detach(UONpc npc) {
        unregisterSpeech(npc.getSerialId());

        var removed = sessions.remove(npc.getSerialId());

        if (removed != null) {
            engine.detach(npc);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AIFlowContext> Optional<AISession<T>> get(UONpc npc) {
        return Optional.ofNullable ((AISession<T>) sessions.get(npc.getSerialId()));
    }

    @Override
    public void detachAll() {

        for (var entry : sessions.entrySet()) {
            unregisterSpeech(entry.getKey());
            try {
                engine.detachById(entry.getKey());
            } catch (Exception e) {
                log.warn("Error detaching AI for npcId={}", entry.getKey(), e);
            }
        }

        sessions.clear();
    }

    private void unregisterSpeech(int npcId) {
        var listener = speechListeners.remove(npcId);
        if (listener != null) eventBus.unregister(MobileSpeech.class, listener);
    }
}
