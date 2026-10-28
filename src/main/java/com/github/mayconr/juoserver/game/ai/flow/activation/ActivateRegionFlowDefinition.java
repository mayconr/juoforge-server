package com.github.mayconr.juoserver.game.ai.flow.activation;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class ActivateRegionFlowDefinition {
    private ActivateRegionFlowDefinition() {}

    public static Flow<ActivateRegionContext> build(AIModule ai) {
        return FlowFactory.<ActivateRegionContext>builder()
                .step(new ActivateRegionStep(ai))
                .build();
    }
}
