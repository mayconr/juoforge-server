package com.github.mayconr.juoserver.game.session.world.speech;

import com.github.mayconr.juoserver.game.model.UOMobile;

public interface SpeechRateLimiter {
    boolean allow(UOMobile mobile);
}
