package com.github.mayconr.juoserver.game.vitals;

import com.github.mayconr.juoserver.game.model.UOMobile;

/** Resource regeneration operations; intervals are measured in seconds. */
public interface WorldVitals {
    void regen(UOMobile mobile, double elapsedSeconds);
}
