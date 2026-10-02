package com.github.mayconr.juoserver.game.random;

import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RandomModuleImpl implements RandomModule {
    private final RNG rng;

    @Override
    public boolean roll(double chance) {
        return rng.roll(chance);
    }
}
