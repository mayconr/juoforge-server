package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;

public interface ModuleContext {

    FlowFacade flows();

    interface FlowFacade {
        <T extends AbstractContext> void execute(T context);
    }
}
