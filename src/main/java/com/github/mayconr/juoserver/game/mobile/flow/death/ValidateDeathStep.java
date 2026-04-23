package com.github.mayconr.juoserver.game.mobile.flow.death;

import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition.DeathContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateDeathStep extends AbstractFlowStep<DeathContext> {
    public ValidateDeathStep() {
        super("validate_death_step", 100, FlowPhase.CORE);
    }

    @Override
    public StepResult execute(DeathContext context) {
        if (context.getVictim() == null) {
            return StepResult.failure("Mobile victim is null");
        }
        if (context.getKiller() == null) {
            return StepResult.failure("Killer is null");
        }
        if (context.getCause() == null) {
            return StepResult.failure("Cause is null");
        }
        if (!context.getVictim().isAlive()) {
            return StepResult.failure("Mobile [" + context.getVictim().getName() + "] is already dead");
        }

        return StepResult.success();
    }
}
