package com.github.mayconr.juoserver.game.policy;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;

import java.time.Clock;

public interface ActionContext<T extends ActionPolicy> {
    UOMobile mobile();
    WorldInternal world();
    Clock clock();
    T action();
}
