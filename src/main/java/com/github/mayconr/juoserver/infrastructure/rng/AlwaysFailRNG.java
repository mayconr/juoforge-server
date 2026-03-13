package com.github.mayconr.juoserver.infrastructure.rng;

public class AlwaysFailRNG implements RNG {
    @Override
    public boolean roll(double chance) {
        return false;
    }
}
