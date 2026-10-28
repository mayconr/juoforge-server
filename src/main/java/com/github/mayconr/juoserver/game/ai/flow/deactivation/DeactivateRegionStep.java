package com.github.mayconr.juoserver.game.ai.flow.deactivation;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.region.WorldRegion;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DeactivateRegionStep extends AbstractFlowStep<DeactivateRegionContext> {
    private final WorldRegion regions;
    private final AIModule ai;

    public DeactivateRegionStep(WorldRegion regions, AIModule ai) {
        super("DeactivateRegion");
        this.regions = regions;
        this.ai = ai;
    }

    @Override
    public StepResult execute(DeactivateRegionContext context) {
        var region = context.getRegion();
        if (!regions.getPlayerSerials(region).isEmpty()) return StepResult.stop();
        int deactivated = 0;
        for (var npc : context.getNpcs()) {
            if (region.contains(npc)) {
                ai.deactivate(npc);
                deactivated++;
            }
        }
        log.debug("AI deactivation completed in region [{}]: {} NPCs", region.getName(), deactivated);
        return StepResult.success();
    }
}
