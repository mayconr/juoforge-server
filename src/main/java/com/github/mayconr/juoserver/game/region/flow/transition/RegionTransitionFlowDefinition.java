package com.github.mayconr.juoserver.game.region.flow.transition;

import com.github.mayconr.juoserver.game.region.RegionModule;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;

public final class RegionTransitionFlowDefinition {
    private RegionTransitionFlowDefinition() {}

    public static Flow<RegionTransitionContext> build(RegionSystem regions, EventBus events, RegionModule module, AIModule aiModule) {
        return FlowFactory.<RegionTransitionContext>builder()
                .step(new RegionTransitionStep(regions, events, module))
                .step(new UpdateRegionAiStep(regions, aiModule))
                .build();
    }
}
