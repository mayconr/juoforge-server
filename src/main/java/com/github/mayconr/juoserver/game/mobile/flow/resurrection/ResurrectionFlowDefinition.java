package com.github.mayconr.juoserver.game.mobile.flow.resurrection;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class ResurrectionFlowDefinition {
    private ResurrectionFlowDefinition() {}

    public static Flow<ResurrectionContext> build(EventBus events) {
        return FlowFactory.<ResurrectionContext>builder()
                .step(new ValidateResurrectionStep())
                .step(new RestorePlayerLifeStep())
                .step(new NotifyResurrectionStep(events))
                .build();
    }
}

