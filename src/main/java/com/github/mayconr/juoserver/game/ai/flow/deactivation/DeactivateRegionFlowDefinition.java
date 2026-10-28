package com.github.mayconr.juoserver.game.ai.flow.deactivation;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.region.WorldRegion;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class DeactivateRegionFlowDefinition {
    private DeactivateRegionFlowDefinition() {}

    public static Flow<DeactivateRegionContext> build(WorldRegion regions, AIModule ai) {
        return FlowFactory.<DeactivateRegionContext>builder()
                .step(new DeactivateRegionStep(regions, ai))
                .build();
    }
}
