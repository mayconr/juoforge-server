package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.actions.NpcAction;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.definition.PassiveAnimalAIContext;
import com.github.mayconr.juoserver.game.ai.definition.VendorAIContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class AIEngineImpl implements AIEngine {
    private final Map<Integer, AISession<?>> sessions = new ConcurrentHashMap<>();

    private final World world;
    private final Consumer<NpcAction> actionDispatcher;
    private ModuleContext.FlowFacade flows;

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

        AISession<AIFlowContext> session = new AISessionImpl<>(flows, context, actionDispatcher);

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
        log.info("All AI sessions detached");
    }

    // =========================
    // Game loop
    // =========================

    @Override
    public void update(double delta) {

        // loop simples e rápido
        for (var session : sessions.values()) {
            session.update(delta);
        }
    }

    // =========================
    // Context factory
    // =========================

    private AIFlowContext createContext(UONpc npc, String aiType) {

        return switch (aiType) {

            case "DIALOGUE_REACTIVE" -> {
                var stockType = (String) npc.persistentAttributes()
                        .get("behavior.stockType");

                yield new VendorAIContext(npc, world, stockType);
            }

            case "PASSIVE_ANIMAL" -> new PassiveAnimalAIContext(npc, world);

            case "banker" -> null;

            default -> new AIFlowContext(npc, world);
        };
    }
}
