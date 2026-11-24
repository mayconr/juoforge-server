package com.github.mayconr.juoserver.game.region.flow.transition;

import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class TeleportRegionStep extends AbstractFlowStep<RegionTransitionContext> {
    private final WorldMobile mobiles;

    public TeleportRegionStep(WorldMobile mobiles) {
        super("teleport-region");
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(RegionTransitionContext context) {
        var destination = context.getNewRegion().getTeleport().destination().toLocation();
        mobiles.teleport(context.getMobile(), destination);
        return StepResult.success();
    }
}
