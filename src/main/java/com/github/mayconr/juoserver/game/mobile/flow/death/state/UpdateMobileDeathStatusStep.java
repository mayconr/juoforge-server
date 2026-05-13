package com.github.mayconr.juoserver.game.mobile.flow.death.state;

import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class UpdateMobileDeathStatusStep extends AbstractFlowStep<DeathContext> {
    public UpdateMobileDeathStatusStep() {
        super("update_mobile_death_status");
    }

    @Override
    public StepResult execute(DeathContext context) {
        context.getVictim().setAlive(false);
        return StepResult.success();
    }
}
