package com.github.mayconr.juoserver.game.combat.flow.execution.validation;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateTargetDistanceStep extends AbstractFlowStep<CombatExecutionContext> {
    public ValidateTargetDistanceStep() {
        super("ValidateTargetDistanceStep");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        final var session = context.getSession();
        final var attacker = session.getAttacker();
        final var target = session.getTarget();

        if (!GameMath.isInRange(attacker, target, context.getCombatRadius())) {
            return StepResult.stop("COMBAT_OUT_OF_RANGE", "Target is out of range");
        }
        return StepResult.success();
    }
}
