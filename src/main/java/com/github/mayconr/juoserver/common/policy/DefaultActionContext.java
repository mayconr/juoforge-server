package com.github.mayconr.juoserver.common.policy;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.world.WorldSession;

import java.time.Clock;

public final class DefaultActionContext implements ActionContext {

    private final UOMobile mobile;
    private final WorldSession world;
    private final Clock clock;

    public DefaultActionContext(
            UOMobile mobile,
            WorldSession world,
            Clock clock
    ) {
        this.mobile = mobile;
        this.world = world;
        this.clock = clock;
    }

    @Override
    public UOMobile mobile() {
        return mobile;
    }

    @Override
    public WorldSession world() {
        return world;
    }

    @Override
    public Clock clock() {
        return clock;
    }
}
