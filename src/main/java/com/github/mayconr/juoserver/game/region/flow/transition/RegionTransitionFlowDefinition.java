package com.github.mayconr.juoserver.game.region.flow.transition;

import com.github.mayconr.juoserver.game.region.RegionModule;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.RegionType;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;

public final class RegionTransitionFlowDefinition {
    private RegionTransitionFlowDefinition() {}

    public static Flow<RegionTransitionContext> build(RegionSystem regions, EventBus events, RegionModule module, AIModule aiModule, WorldMobile mobiles) {
        return FlowFactory.<RegionTransitionContext>builder()
                .step(new RegionTransitionStep(regions, events, module))
                .step(new UpdateRegionAiStep(regions, aiModule))
                .step(new TeleportRegionStep(mobiles), context -> context.getOldRegion() != null
                        && context.getNewRegion() != null
                        && !context.getOldRegion().equals(context.getNewRegion())
                        && context.getNewRegion().getType() == RegionType.TELEPORT)
                .build();
    }
}
