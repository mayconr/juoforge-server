package com.github.mayconr.juoserver.common.policy;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.world.WorldSession;

import java.time.Clock;

public interface ActionContext {
    UOMobile mobile();
    WorldSession world();
    Clock clock();
}
