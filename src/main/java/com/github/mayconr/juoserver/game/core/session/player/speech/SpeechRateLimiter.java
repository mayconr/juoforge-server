package com.github.mayconr.juoserver.game.core.session.player.speech;

import com.github.mayconr.juoserver.game.core.model.UOMobile;

public interface SpeechRateLimiter {
    boolean allow(UOMobile mobile);
}
