package com.github.mayconr.juoserver.game.npc.profile;

import com.github.mayconr.juoserver.game.npc.behavior.NpcBehavior;

/**
 * Defines a set of behaviors available for a specific NPC role.
 * <p>
 * A {@code BehaviorProfile} groups related {@link NpcBehavior} implementations
 * (such as idle, talk, or service) and is used by the NPC AI to decide
 * which behavior should be active at a given moment.
 * </p>
 *
 * <p>
 * Implementations are typically role-based (e.g. banker, vendor, guard)
 * and provide preconfigured behavior instances without embedding
 * decision logic.
 * </p>
 *
 * <p>
 * Lifecycle:
 * <ul>
 *   <li>The profile is created when the NPC session is initialized.</li>
 *   <li>The AI selects a behavior from this profile via {@code decide(...)}.</li>
 *   <li>The selected behavior is initialized and becomes the current behavior.</li>
 * </ul>
 * </p>
 */
public interface BehaviorProfile {

    /**
     * Returns the default idle behavior.
     * <p>
     * Used when the NPC has no active interaction or goal.
     * Typical actions include standing still, wandering, or ambient animations.
     * </p>
     *
     * @return the idle behavior
     */
    NpcBehavior idle();

    /**
     * Returns the talk behavior.
     * <p>
     * Used for conversational or reactive interactions, such as responding
     * to player speech without providing a service.
     * </p>
     *
     * @return the talk behavior
     */
    NpcBehavior talk();

    /**
     * Returns the service behavior.
     * <p>
     * Used when the NPC is actively providing a service to a player
     * (e.g. banking, trading, quest handling).
     * </p>
     *
     * @return the service behavior
     */
    NpcBehavior service();
}
