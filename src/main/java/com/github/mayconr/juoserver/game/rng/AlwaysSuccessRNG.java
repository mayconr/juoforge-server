package com.github.mayconr.juoserver.game.rng;

public class AlwaysSuccessRNG implements RNG {

    @Override
    public boolean roll(double chance) {
        return true;
    }
}
