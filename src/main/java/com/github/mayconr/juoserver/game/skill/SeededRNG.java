package com.github.mayconr.juoserver.game.skill;

import java.util.Random;

public class SeededRNG implements RNG {

    private final Random random;

    public SeededRNG(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public boolean roll(double chance) {
        return random.nextDouble() < chance;
    }
}
