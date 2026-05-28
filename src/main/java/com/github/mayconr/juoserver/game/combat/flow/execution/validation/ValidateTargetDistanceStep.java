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
        final var weapon = context.getWeapon().getTemplate().weapon();

        if (!GameMath.isInRange(attacker, target, weapon.radius())) {
            System.out.println("out of range");
            return StepResult.failure("Out of Range");
        }
        return StepResult.success();
    }
}
