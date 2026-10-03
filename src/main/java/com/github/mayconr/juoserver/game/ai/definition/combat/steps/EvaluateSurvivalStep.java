package com.github.mayconr.juoserver.game.ai.definition.combat.steps;

import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Placeholder: Decide whether to flee or resume normal behavior. */
public final class EvaluateSurvivalStep extends AbstractFlowStep<CombatAIContext> {
    public EvaluateSurvivalStep() {
        super("EvaluateSurvivalStep");
    }

    @Override
    public StepResult execute(CombatAIContext context) {
        // TODO Implement the decision and its actions.
        return StepResult.skip();
    }
}
