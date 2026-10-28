package com.github.mayconr.juoserver.game.region.flow.transition;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;

public final class UpdateRegionAiStep extends AbstractFlowStep<RegionTransitionContext> {
    private final RegionSystem regions;
    private final AIModule aiModule;

    public UpdateRegionAiStep(RegionSystem regions, AIModule aiModule) {
        super("UpdateRegionAi");
        this.regions = regions;
        this.aiModule = aiModule;
    }

    @Override
    public StepResult execute(RegionTransitionContext context) {
        var oldRegion = context.getOldRegion();
        var newRegion = context.getNewRegion();
        if (context.getMobile() instanceof UOPlayer
                && !java.util.Objects.equals(oldRegion, newRegion)) {
            var exited = regions.findAiActivationRegion(oldRegion).orElse(null);
            var entered = regions.findAiActivationRegion(newRegion).orElse(null);
            if (exited != null && !exited.equals(entered)) aiModule.deactivateRegion(exited);
            if (entered != null) aiModule.activateRegion(entered);
        }
        return StepResult.success();
    }
}
