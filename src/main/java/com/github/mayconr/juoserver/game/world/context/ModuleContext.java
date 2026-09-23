package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public interface ModuleContext {

    FlowFacade flows();

    interface FlowFacade extends com.github.mayconr.juoserver.infrastructure.flow.FlowExecutor {}
}
