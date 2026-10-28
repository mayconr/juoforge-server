package com.github.mayconr.juoserver.game.ai.engine;

import com.github.mayconr.juoserver.game.ai.session.AISession;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.ai.registry.AIContextFactory;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;

import com.github.mayconr.juoserver.game.ai.AIStatus;
import java.util.Set;

public interface AIEngine {

    void registerAi(String type, AIContextFactory<?> factory);

    Set<String> getRegisteredTypes();


    void initialize(FlowFacade flows);

    <T extends AIFlowContext> AISession<T> attach(UONpc npc);

    /** Activates an attached AI session; repeated calls have no effect. */
    void activate(UONpc npc);

    void deactivate(UONpc npc);

    void detach(UONpc npc);

    void detachById(int npcId);

    AIStatus getAiStatus(UONpc npc);

    /** Returns an immutable snapshot of NPC serials with activated AI. */
    Set<Integer> getActiveNpcSerials();

    void detachAll();

    void update(double delta);
}
