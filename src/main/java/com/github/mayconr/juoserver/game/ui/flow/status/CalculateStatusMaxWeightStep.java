package com.github.mayconr.juoserver.game.ui.flow.status;

import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class CalculateStatusMaxWeightStep extends AbstractFlowStep<StatusGumpContext> {
    private final WorldMobile mobiles;

    public CalculateStatusMaxWeightStep(WorldMobile mobiles) {
        super("calculate-status-max-weight");
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(StatusGumpContext context) {
        context.setMaxWeight(mobiles.calculateMaxWeight(context.getMobile()));
        return StepResult.success();
    }
}
