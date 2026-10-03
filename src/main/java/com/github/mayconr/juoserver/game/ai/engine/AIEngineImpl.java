package com.github.mayconr.juoserver.game.ai.engine;

import com.github.mayconr.juoserver.game.ai.session.AISession;
import com.github.mayconr.juoserver.game.ai.session.AISessionImpl;
import com.github.mayconr.juoserver.game.ai.policy.AIActivationPolicy;
import com.github.mayconr.juoserver.game.ai.policy.AISpeechPolicy;

import com.github.mayconr.juoserver.game.ai.actions.NpcAction;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.ai.definition.animal.PassiveAnimalAIContext;
import com.github.mayconr.juoserver.game.ai.definition.vendor.VendorAIContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class AIEngineImpl implements AIEngine {
    private final Map<Integer, AISession<?>> sessions = new ConcurrentHashMap<>();

    private final World world;
    private final Consumer<NpcAction> actionDispatcher;
    private final AIActivationPolicy activationPolicy;
    private final AISpeechPolicy speechPolicy;
    private final GamePlaySettings.Ai settings;
    private double accumulatedDelta;
    private ModuleContext.FlowFacade flows;

    public AIEngineImpl(World world, Consumer<NpcAction> actionDispatcher,
                        AIActivationPolicy activationPolicy, AISpeechPolicy speechPolicy,
                        GamePlaySettings.Ai settings) {
        this.world = world;
        this.actionDispatcher = actionDispatcher;
        this.activationPolicy = activationPolicy;
        this.speechPolicy = speechPolicy;
        this.settings = Objects.requireNonNull(settings, "AI settings are required");
    }

    @Override
    public void initialize(ModuleContext.FlowFacade flows) {
        this.flows = flows;
    }

    // =========================
    // Lifecycle
    // =========================
    @SuppressWarnings("unchecked")
    @Override
    public <T extends AIFlowContext> AISession<T> attach(UONpc npc) {
        if (flows == null) {
            throw new IllegalStateException("flows not initialized");
        }
        int id = npc.getSerialId();

        if (sessions.containsKey(id)) {
            return (AISession<T>) sessions.get(id);
        }

        String aiType = npc.getBehavior().ai();

        AIFlowContext context = createContext(npc, aiType);

        AISession<AIFlowContext> session = new AISessionImpl<>(flows, context, actionDispatcher,
                activationPolicy, speechPolicy);

        sessions.put(id, session);

        log.info("AI attached [{}] for NPC [{}]", aiType, npc.getName());

        return (AISession<T>) session;
    }

    @Override
    public void detach(UONpc npc) {
        detachById(npc.getSerialId());
    }

    @Override
    public void detachById(int npcId) {

        var removed = sessions.remove(npcId);

        if (removed != null) {
            log.info("AI detached for npcId={}", npcId);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AIFlowContext> Optional<AISession<T>> get(UONpc npc) {
        return Optional.ofNullable((AISession<T>) sessions.get(npc.getSerialId()));
    }

    @Override
    public void detachAll() {
        sessions.clear();
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
        if (sessions.isEmpty()) {
            accumulatedDelta = 0;
            return;
        }
        accumulatedDelta += delta;
        if (accumulatedDelta < settings.updateIntervalSeconds()) return;

        // Execute once, even after a slow tick; flows receive the actual elapsed time.
        double elapsed = accumulatedDelta;
        accumulatedDelta = 0;
        for (var session : sessions.values()) {
            session.update(elapsed);
        }
    }

    // =========================
    // Context factory
    // =========================

    private AIFlowContext createContext(UONpc npc, String aiType) {

        return switch (aiType) {

            case "DIALOGUE_REACTIVE" -> {
                var stockType = npc.getStockType();

                yield new VendorAIContext(npc, world, stockType);
            }

            case "PASSIVE_ANIMAL" -> new PassiveAnimalAIContext(npc, world);
            case "COMBAT" -> new CombatAIContext(npc, world);

            case "banker" -> null;

            default -> new AIFlowContext(npc, world);
        };
    }
}
