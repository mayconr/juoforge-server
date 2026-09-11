package com.github.mayconr.juoserver.game.combat.flow.execution.resolver;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class MountResolverStep extends AbstractFlowStep<CombatExecutionContext> {
    protected MountResolverStep() {
        super("MountResolverStep");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        var attacker = context.getSession().getAttacker();

        context.setMounted(attacker.isMounted());

        return StepResult.success();
    }
}
