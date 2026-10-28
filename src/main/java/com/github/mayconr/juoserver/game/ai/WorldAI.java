package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.session.AISession;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;

import java.util.Optional;

public interface WorldAI {

    /** Attaches an inactive session, or returns the existing session without changing its state. */
    <T extends AIFlowContext> AISession<T> attach(UONpc npc);

    /**
     * Enables an attached session for future updates and speech handling.
     * Activation policies still control whether it can act. Repeated calls do not reset its state.
     *
     * @param npc the NPC whose attached session will be activated
     * @throws IllegalStateException if the NPC has no attached session
     */
    void activate(UONpc npc);

    void detach(UONpc npc);

    <T extends AIFlowContext> Optional<AISession<T>> get(UONpc npc);

    void detachAll();
}
