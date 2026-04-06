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
            log.debug("Mobile victim is null");
            return StepResult.STOP;
        }
        if (context.getKiller() == null) {
            log.debug("Object killer is null");
            return StepResult.STOP;
        }
        if (context.getCause() == null) {
            log.debug("Cause is null");
            return StepResult.STOP;
        }
        if (!context.getVictim().isAlive()) {
            log.debug("Mobile [{}] is already dead", context.getVictim().getName());
            return StepResult.STOP;
        }

        return StepResult.CONTINUE;
    }
}
