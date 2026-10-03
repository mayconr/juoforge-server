package com.github.mayconr.juoserver.game.combat.flow.execution.resolver;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.CombatAttackRange;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveCombatRadius extends AbstractFlowStep<CombatExecutionContext> {
    public ResolveCombatRadius() {
        super("ResolveCombatRadius");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        context.setCombatRadius(CombatAttackRange.forWeapon(context.getWeapon()));

        return StepResult.success();
    }
}
