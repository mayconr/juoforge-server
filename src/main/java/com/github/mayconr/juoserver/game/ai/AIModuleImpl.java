package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.engine.AIEngine;
import com.github.mayconr.juoserver.game.ai.registry.AIContextFactory;
import com.github.mayconr.juoserver.game.ai.session.AISession;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.game.ai.flow.deactivation.DeactivateRegionContext;
import com.github.mayconr.juoserver.game.ai.flow.activation.ActivateRegionContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class AIModuleImpl implements AIModule {
    private final AIEngine engine;
    private final EventBus eventBus;
    private final Map<Integer, AISession<?>> sessions = new ConcurrentHashMap<>();
    private final Map<Integer, UONpc> attachedNpcs = new ConcurrentHashMap<>();
    private final Map<Integer, EventHandler<MobileSpeech>> speechListeners = new ConcurrentHashMap<>();
    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        flows = context.flows();
        engine.initialize(context.flows());
        log.info("AI Module initialized");
    }

    @Override
    public void update(double delta) {
        engine.update(delta);
    }

    @Override
    public void registerAi(String type, AIContextFactory<?> factory) {
        engine.registerAi(type, factory);
    }

    @Override
    public Set<String> getRegisteredTypes() {
        return engine.getRegisteredTypes();
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
        attachedNpcs.put(npc.getSerialId(), npc);

        log.info("Session attached to AI Module");

        return (AISession<T>) session;
    }

    @Override
    public void activate(UONpc npc) {
        engine.activate(npc);
    }

    @Override
    public void activateRegion(RegionNode region) {
        var result = flows.execute(new ActivateRegionContext(region, List.copyOf(attachedNpcs.values())));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("activateRegion", result, null);
        }
    }

    @Override
    public void deactivateRegion(RegionNode region) {
        var result = flows.execute(new DeactivateRegionContext(region, List.copyOf(attachedNpcs.values())));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("deactivateRegion", result, null);
        }
    }

    @Override
    public void deactivate(UONpc npc) {
        engine.deactivate(npc);
    }

    @Override
    public void detach(UONpc npc) {
        attachedNpcs.remove(npc.getSerialId());
        unregisterSpeech(npc.getSerialId());

        var removed = sessions.remove(npc.getSerialId());

        if (removed != null) {
            engine.detach(npc);
        }
    }

    @Override
    public AIStatus getAiStatus(UONpc npc) {
        return engine.getAiStatus(npc);
    }

    @Override
    public List<UONpc> getActiveNpcs() {
        return engine.getActiveNpcSerials().stream()
                .map(attachedNpcs::get)
                .filter(java.util.Objects::nonNull)
                .toList();
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
        attachedNpcs.clear();
    }

    private void unregisterSpeech(int npcId) {
        var listener = speechListeners.remove(npcId);
        if (listener != null) eventBus.unregister(MobileSpeech.class, listener);
    }
}
