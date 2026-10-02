package com.github.mayconr.juoserver.game.random;

/** Probability checks available through the world's configured random generator. */
public interface WorldRandom {
    /** @param chance probability between 0.0 and 1.0 */
    boolean roll(double chance);
}
