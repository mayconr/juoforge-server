package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.session.AISession;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.ai.registry.AIContextFactory;

import java.util.List;
import java.util.Set;

public interface WorldAI {

    /**
     * Registers a context factory for a new, case-sensitive NPC behavior AI type.
     * Register the corresponding context flow before attaching NPCs of this type.
     * Existing sessions are not changed.
     *
     * @param type the value used by the NPC AI profile
     * @param factory creates a context from the NPC and world
     * @throws IllegalArgumentException if the type is blank or already registered
     */
    void registerAi(String type, AIContextFactory<?> factory);

    /** Returns an immutable snapshot of all registered AI types, including server and shard types. */
    Set<String> getRegisteredTypes();


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

    /** Returns the installed AI type and whether it is installed and activated. */
    AIStatus getAiStatus(UONpc npc);

    /**
     * Returns an immutable snapshot of NPCs whose AI is activated.
     * Activation policies may still prevent these NPCs from acting.
     *
     * @return NPCs with activated AI, excluding inactive and detached sessions
     */
    List<UONpc> getActiveNpcs();

    void detachAll();
}
