package com.github.mayconr.juoserver.game.ai.decision;

import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.behavior.Behavior;
import com.github.mayconr.juoserver.game.ai.profile.BehaviorProfile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

/**
 * Defines the decision-making logic for an NPC.
 * <p>
 * An {@code NpcAI} is responsible for selecting the appropriate
 * {@link Behavior} based on the current {@link AIContext}
 * and the available behaviors provided by a {@link BehaviorProfile}.
 * </p>
 *
 * <p>
 * The AI does not execute actions directly. Instead, it evaluates
 * contextual information (such as recent events, targets, or interaction
 * state) and decides which behavior should be active.
 * </p>
 *
 * <p>
 * Typical responsibilities include:
 * <ul>
 *   <li>Interpreting events and updating internal decision state</li>
 *   <li>Choosing behaviors in response to context changes</li>
 *   <li>Switching behaviors when conditions change</li>
 * </ul>
 * </p>
 *
 * <p>
 * The AI is usually notified of game events through {@link #onEvent},
 * allowing it to react or record information without directly coupling
 * to specific event types.
 * </p>
 */
public interface NpcAI {

    String getKey();

    /**
     * Decides which behavior should be active for the NPC.
     * <p>
     * This method is typically called after relevant context changes
     * or periodically during the game loop.
     * </p>
     *
     * @param ctx the current NPC context containing dynamic state and memory
     * @param profile the behavior profile providing available behaviors
     * @return the behavior that should become active
     */
    Behavior decide(AIContext ctx, BehaviorProfile profile);

    /**
     * Notifies the AI of a game event.
     * <p>
     * Implementations may use this callback to observe events, update
     * decision-related state, or store information in the context.
     * The default implementation does nothing.
     * </p>
     *
     * <p>
     * This method should not execute game actions directly.
     * </p>
     *
     * @param ctx the current NPC context
     * @param event the game event being observed
     */
    default void onEvent(AIContext ctx, GameEvent event) {}
}
