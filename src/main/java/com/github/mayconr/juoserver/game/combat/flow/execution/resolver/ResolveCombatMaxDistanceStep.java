package com.github.mayconr.juoserver.game.combat.flow.execution.resolver;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveCombatMaxDistanceStep extends AbstractFlowStep<CombatExecutionContext> {
    public ResolveCombatMaxDistanceStep() {
        super("ResolveCombatDistanceStep");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        return StepResult.success();
    }
}
