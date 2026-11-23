package com.github.mayconr.juoserver.game.combat.flow.execution.swing;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ReserveCombatSwingStep extends AbstractFlowStep<CombatExecutionContext> {
    public ReserveCombatSwingStep() {
        super("ReserveCombatSwing");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        if (context.isFromMovement() && !context.isContactAttack()) {
            return StepResult.stop();
        }
        return context.reserveSwing() ? StepResult.success() : StepResult.stop();
    }
}
