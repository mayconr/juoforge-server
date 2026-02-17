package com.github.mayconr.juoserver.game.ai.behavior;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.decision.NpcAI;
import com.github.mayconr.juoserver.game.ai.action.NpcAction;

/**
 * Represents an executable behavior for an NPC.
 * <p>
 * A {@code NpcBehavior} defines how an NPC reacts to events and time progression
 * once it has been selected by the {@link NpcAI}. Behaviors are stateful and
 * typically short-lived, representing a specific mode such as idling,
 * talking, servicing a player, or engaging in combat.
 * </p>
 *
 * <p>
 * A behavior does not decide when it should be active and does not execute
 * game logic directly. Instead, it reacts to callbacks and enqueues
 * {@link NpcAction actions} through the {@link AIContext}.
 * </p>
 *
 * <p>
 * Lifecycle:
 * <ul>
 *   <li>{@link #initialize(AIContext)} is called when the behavior becomes active.</li>
 *   <li>Event callbacks (such as speech or interaction) are forwarded to the behavior.</li>
 *   <li>{@link #onThink(double)} is called periodically by the game loop.</li>
 *   <li>{@link #onExit(AIContext)} is called when the behavior is replaced.</li>
 * </ul>
 * </p>
 */
public interface Behavior {

    /**
     * Initializes the behavior.
     * <p>
     * Called when this behavior becomes the current active behavior for the NPC.
     * Implementations may set up internal state or write initial values
     * to the {@link AIContext}.
     * </p>
     *
     * @param context the NPC context shared across AI, behaviors, and events
     */
    void initialize(AIContext context);

    /**
     * Called when a player speaks within interaction range of the NPC.
     * <p>
     * Implementations may react by enqueuing actions such as responding with
     * speech, changing state, or requesting further input.
     * </p>
     *
     * @param player the player who spoke
     * @param text the spoken text
     */
    default void onSpeech(UOPlayer player, String text) {};

    /**
     * Called when a player directly interacts with the NPC
     * (e.g. double-clicks or uses an interaction command).
     *
     * @param player the interacting player
     */
    default void onInteract(UOPlayer player) {};

    /**
     * Called when this behavior is about to be replaced by another behavior.
     * <p>
     * Implementations may use this callback to clean up state,
     * release resources, or update the {@link AIContext}.
     * </p>
     *
     * @param context the NPC context
     */
    default void onExit(AIContext context) {};

    /**
     * Called periodically by the game loop while this behavior is active.
     * <p>
     * This method is used for continuous or time-based logic, such as movement,
     * cooldown checks, or long-running interactions.
     * </p>
     *
     * @param delta the elapsed time since the last think call, in seconds
     */
    void onThink(double delta);
}
