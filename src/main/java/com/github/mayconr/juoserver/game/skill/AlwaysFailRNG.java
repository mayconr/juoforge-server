package com.github.mayconr.juoserver.game.skill;

class AlwaysFailRNG implements RNG {
    @Override
    public boolean roll(double chance) {
        return false;
    }
}
