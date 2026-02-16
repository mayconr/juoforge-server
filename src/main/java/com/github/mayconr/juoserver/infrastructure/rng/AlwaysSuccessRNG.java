package com.github.mayconr.juoserver.infrastructure.rng;

public class AlwaysSuccessRNG implements RNG {

    @Override
    public boolean roll(double chance) {
        return true;
    }
}
