package com.github.mayconr.juoserver.game.region.shared;

import com.github.mayconr.juoserver.game.region.RegionModule;
import com.github.mayconr.juoserver.infrastructure.flow.FlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class UpdateMobileRegionStep<T extends UpdateMobileRegionContext> implements FlowStep<T> {
    private final RegionModule regions;

    @Override
    public String name() {
        return "UpdateMobileRegion";
    }

    @Override
    public StepResult execute(T context) {
        regions.regionTransition(context.mobile(), context.regionFrom(), context.regionTo());
        return StepResult.success();
    }
}
