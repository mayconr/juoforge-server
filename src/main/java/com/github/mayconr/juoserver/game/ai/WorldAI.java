package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;

import java.util.Optional;

public interface WorldAI {

    <T extends AIFlowContext> AISession<T> attach(UONpc npc);

    void detach(UONpc npc);

    <T extends AIFlowContext> Optional<AISession<T>> get(UONpc npc);

    void detachAll();
}
