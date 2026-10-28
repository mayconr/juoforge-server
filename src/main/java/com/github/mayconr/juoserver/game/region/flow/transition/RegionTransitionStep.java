package com.github.mayconr.juoserver.game.region.flow.transition;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileRegionChanged;
import com.github.mayconr.juoserver.game.region.RegionModule;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;

public final class RegionTransitionStep extends AbstractFlowStep<RegionTransitionContext> {
    private final RegionSystem regions;
    private final EventBus events;
    private final RegionModule module;

    public RegionTransitionStep(RegionSystem regions, EventBus events, RegionModule module) {
        super("RegionTransition");
        this.regions = regions;
        this.events = events;
        this.module = module;
    }

    @Override
    public StepResult execute(RegionTransitionContext context) {
        var oldRegion = context.getFrom() == null ? null : regions.getRegion(context.getFrom()).orElse(null);
        var newRegion = context.getTo() == null ? null : regions.getRegion(context.getTo()).orElse(null);
        context.setOldRegion(oldRegion);
        context.setNewRegion(newRegion);
        if (context.getMobile() instanceof UOPlayer player) {
            module.updatePlayerRegion(player, newRegion);
        }
        if (oldRegion != null && newRegion != null && !oldRegion.equals(newRegion)) {
            events.publish(new MobileRegionChanged(context.getMobile(), oldRegion, newRegion));
        }
        return StepResult.success();
    }
}
