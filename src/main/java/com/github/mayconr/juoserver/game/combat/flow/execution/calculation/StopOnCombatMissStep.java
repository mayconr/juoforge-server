package com.github.mayconr.juoserver.game.combat.flow.execution.calculation;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.CombatHitResult;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.Objects;

/** Stops missed swings after skill gain processing and before damage calculation. */
public class StopOnCombatMissStep extends AbstractFlowStep<CombatExecutionContext> {
    public StopOnCombatMissStep() {
        super("StopOnCombatMissStep");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        return Objects.requireNonNull(context.getHitResult(), "Hit result is not resolved") == CombatHitResult.MISS
                ? StepResult.stop("COMBAT_MISS", "Attack missed")
                : StepResult.success();
    }
}
