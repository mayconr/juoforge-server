package com.github.mayconr.juoserver.game.mobile.flow.setvitals;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class SetVitalsFlowDefinition {
    private SetVitalsFlowDefinition() {}

    public static Flow<SetVitalsContext> build(EventBus eventBus) {
        return FlowFactory.<SetVitalsContext>builder()
                .step(new ApplyVitalsStep())
                .step(new NotifyVitalsStep(eventBus))
                .build();
    }
}
