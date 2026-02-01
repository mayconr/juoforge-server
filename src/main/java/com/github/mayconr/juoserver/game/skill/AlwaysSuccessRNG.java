package com.github.mayconr.juoserver.game.skill;

class AlwaysSuccessRNG implements RNG {

    @Override
    public boolean roll(double chance) {
        return true;
    }
}
