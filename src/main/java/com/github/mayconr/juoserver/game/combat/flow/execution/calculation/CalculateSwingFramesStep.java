package com.github.mayconr.juoserver.game.combat.flow.execution.calculation;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class CalculateSwingFramesStep extends AbstractFlowStep<CombatExecutionContext> {
    public CalculateSwingFramesStep() {
        super("CalculateSwingSpeedStep");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        final var session = context.getSession();

        context.setAnimFrame(1000);
        context.setHitFrame(500);

        return StepResult.success();
    }
}
