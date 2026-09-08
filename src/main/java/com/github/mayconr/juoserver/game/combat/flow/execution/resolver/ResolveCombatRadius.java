package com.github.mayconr.juoserver.game.combat.flow.execution.resolver;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveCombatRadius extends AbstractFlowStep<CombatExecutionContext> {
    public ResolveCombatRadius() {
        super("ResolveCombatRadius");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        if (context.getWeapon() == null) {
            context.setCombatRadius(1);
        } else {
            context.setCombatRadius(context.getWeapon().getTemplate().weapon().radius());
        }

        return StepResult.success();
    }
}
