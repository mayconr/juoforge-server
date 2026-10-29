package com.github.mayconr.juoserver.game.region.flow.transition;

import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class TeleportRegionStep extends AbstractFlowStep<RegionTransitionContext> {
    public static final String TELEPORT_DESTINATION_X = "teleport.destination.x";
    public static final String TELEPORT_DESTINATION_Y = "teleport.destination.y";
    public static final String TELEPORT_DESTINATION_Z = "teleport.destination.z";

    private final WorldMobile mobiles;

    public TeleportRegionStep(WorldMobile mobiles) {
        super("teleport-region");
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(RegionTransitionContext context) {
        var properties = context.getNewRegion().getProperties();
        int x = (int) properties.get(TELEPORT_DESTINATION_X);
        int y = (int) properties.get(TELEPORT_DESTINATION_Y);
        int z = (int) properties.get(TELEPORT_DESTINATION_Z);
        mobiles.teleport(context.getMobile(), new PointInTheWorld(x, y, z));
        return StepResult.success();
    }
}
