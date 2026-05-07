package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;

import java.util.Optional;

public interface AIEngine {

    void initialize(FlowFacade flows);

    <T extends AIFlowContext> AISession<T> attach(UONpc npc);

    void detach(UONpc npc);

    void detachById(int npcId);

    <T extends AIFlowContext> Optional<AISession<T>> get(UONpc npc);

    void detachAll();

    void update(double delta);
}
