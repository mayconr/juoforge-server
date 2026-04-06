package com.github.mayconr.juoserver.game.mobile.flow.death;

import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class UpdateMobileDeathStatusStep extends AbstractFlowStep<DeathFlowDefinition.DeathContext> {
    public UpdateMobileDeathStatusStep() {
        super("update_mobile_death_status", 400, FlowPhase.CORE);
    }

    @Override
    public StepResult execute(DeathFlowDefinition.DeathContext context) {
        context.getVictim().setAlive(false);
        return StepResult.CONTINUE;
    }
}
