package com.github.mayconr.juoserver.game.ai.definition.combat.steps;

import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Placeholder: Cancel combat and move away from threats; stop the flow when handled. */
public final class FleeStep extends AbstractFlowStep<CombatAIContext> {
    public FleeStep() {
        super("FleeStep");
    }

    @Override
    public StepResult execute(CombatAIContext context) {
        // TODO Implement the decision and its actions.
        return StepResult.skip();
    }
}
