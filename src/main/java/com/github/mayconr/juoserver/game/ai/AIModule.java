package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.WorldModule;

import java.util.Optional;

public interface AIModule extends WorldModule {

    <T extends AIFlowContext> AISession<T> attach(UONpc npc);

    void detach(UONpc npc);

    <T extends AIFlowContext> Optional<AISession<T>> get(UONpc npc);

    void detachAll();
}
