package com.github.mayconr.juoserver.game.rng;

/**
 * Abstraction for a random number generator used in probability checks.
 * <p>
 * Commonly used to determine success or failure of game actions such as
 * skill gain, mining, crafting, or loot rolls. This abstraction allows
 * deterministic implementations for testing.
 */
public interface RNG {
    /**
     * Rolls a probability check.
     *
     * @param chance probability between 0.0 and 1.0
     * @return true if the roll succeeds
     */
    boolean roll(double chance);
}
