package com.github.mayconr.juoserver.game.rng;

public class AlwaysFailRNG implements RNG {
    @Override
    public boolean roll(double chance) {
        return false;
    }
}
