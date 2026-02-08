package com.github.mayconr.juoserver.game.rng;

import java.util.concurrent.ThreadLocalRandom;

public class DefaultRNG implements RNG {

    @Override
    public boolean roll(double chance) {
        if (chance <= 0.0) {
            return false;
        }
        if (chance >= 1.0) {
            return true;
        }

        return ThreadLocalRandom.current().nextDouble() < chance;
    }
}
