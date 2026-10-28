package com.github.mayconr.juoserver.game.ai.engine;

import com.github.mayconr.juoserver.WorldCfg;
import com.github.mayconr.juoserver.game.ai.session.AISession;
import com.github.mayconr.juoserver.game.ai.session.AISessionImpl;
import com.github.mayconr.juoserver.game.ai.policy.AIActivationPolicy;
import com.github.mayconr.juoserver.game.ai.policy.AISpeechPolicy;

import com.github.mayconr.juoserver.game.ai.actions.NpcAction;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.registry.AIContextFactory;
import com.github.mayconr.juoserver.game.ai.registry.AIContextRegistry;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import com.github.mayconr.juoserver.game.ai.AIStatus;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class AIEngineImpl implements AIEngine {
    private final Map<Integer, AISession<?>> sessions = new ConcurrentHashMap<>();
    private final Map<Integer, String> aiTypes = new ConcurrentHashMap<>();
    private final Map<Integer, AISession<?>> activeSessions = new ConcurrentHashMap<>();
    private final AIContextRegistry contextRegistry = new AIContextRegistry();

    private final World world;
    private final Consumer<NpcAction> actionDispatcher;
    private final AIActivationPolicy activationPolicy;
    private final AISpeechPolicy speechPolicy;
    private final GamePlaySettings.Ai settings;
    private double accumulatedDelta;
    private ModuleContext.FlowFacade flows;

    public AIEngineImpl(World world, Consumer<NpcAction> actionDispatcher,
                        WorldCfg worldCfg) {
        Objects.requireNonNull(worldCfg, "World configuration is required");
        this.world = world;
        this.actionDispatcher = actionDispatcher;
        this.activationPolicy = Objects.requireNonNull(worldCfg.aiActivationPolicy().get(), "AI activation policy is required");
        this.speechPolicy = Objects.requireNonNull(worldCfg.aiSpeechPolicy().get(), "AI speech policy is required");
        this.settings = Objects.requireNonNull(worldCfg.ai(), "AI settings are required");
    }

    @Override
    public void initialize(ModuleContext.FlowFacade flows) {
        this.flows = flows;
    }

    @Override
    public void registerAi(String type, AIContextFactory<?> factory) {
        contextRegistry.register(type, factory);
    }

    @Override
    public Set<String> getRegisteredTypes() {
        return contextRegistry.getRegisteredTypes();
    }


    // =========================
    // Lifecycle
    // =========================
    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T extends AIFlowContext> AISession<T> attach(UONpc npc) {
        if (flows == null) {
            throw new IllegalStateException("flows not initialized");
        }
        int id = npc.getSerialId();

        if (sessions.containsKey(id)) {
            return (AISession<T>) sessions.get(id);
        }

        String aiType = npc.getBehavior().ai();

        AIFlowContext context = contextRegistry.create(aiType, npc, world);

        var filter = context.speechFilter().orElse(null);
        AISpeechPolicy sessionSpeechPolicy = filter == null ? speechPolicy : (ctx, speech) -> filter.test(speech);
        AISession<AIFlowContext> session = new AISessionImpl<>(flows, context, actionDispatcher,
                activationPolicy, sessionSpeechPolicy);

        sessions.put(id, session);
        aiTypes.put(id, aiType);

        log.info("AI attached [{}] for NPC [{}]", aiType, npc.getName());

        return (AISession<T>) session;
    }

    @Override
    public synchronized void activate(UONpc npc) {
        var session = sessions.get(npc.getSerialId());
        if (session == null) {
            throw new IllegalStateException("No AI attached for npcId=" + npc.getSerialId());
        }
        session.activate();
        activeSessions.put(npc.getSerialId(), session);
    }

    @Override
    public synchronized void deactivate(UONpc npc) {
        var session = activeSessions.remove(npc.getSerialId());
        if (session != null) session.deactivate();
        if (activeSessions.isEmpty()) accumulatedDelta = 0;
    }

    @Override
    public void detach(UONpc npc) {
        detachById(npc.getSerialId());
    }

    @Override
    public synchronized void detachById(int npcId) {

        activeSessions.remove(npcId);
        var removed = sessions.remove(npcId);
        aiTypes.remove(npcId);

        if (removed != null) {
            log.info("AI detached for npcId={}", npcId);
        }
    }

    @Override
    public synchronized AIStatus getAiStatus(UONpc npc) {
        int serial = npc.getSerialId();
        var type = aiTypes.get(serial);
        return new AIStatus(type, type != null, activeSessions.containsKey(serial));
    }

    @Override
    public synchronized Set<Integer> getActiveNpcSerials() {
        return Set.copyOf(activeSessions.keySet());
    }

    @Override
    public synchronized void detachAll() {
        activeSessions.clear();
        sessions.clear();
        aiTypes.clear();
        accumulatedDelta = 0;
        log.info("All AI sessions detached");
    }

    // =========================
    // Game loop
    // =========================

    @Override
    public void update(double delta) {
        if (!Double.isFinite(delta) || delta < 0) {
            throw new IllegalArgumentException("delta must be finite and non-negative");
        }
        if (activeSessions.isEmpty()) {
            accumulatedDelta = 0;
            return;
        }
        accumulatedDelta += delta;
        if (accumulatedDelta < settings.updateIntervalSeconds()) return;

        // Execute once, even after a slow tick; flows receive the actual elapsed time.
        double elapsed = accumulatedDelta;
        accumulatedDelta = 0;
        for (var session : activeSessions.values()) {
            session.update(elapsed);
        }
    }

}
