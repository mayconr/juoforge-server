package com.github.mayconr.juoserver.game.skill;

public interface RNG {
    /**
     * Rolls a probability check.
     *
     * @param chance probability between 0.0 and 1.0
     * @return true if the roll succeeds
     */
    boolean roll(double chance);
}
