package com.github.mayconr.juoserver.game.ai.definition.combat.steps;

import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Placeholder: Wait in safety until recovered; stop the flow when handled. */
public final class RecoverStep extends AbstractFlowStep<CombatAIContext> {
    public RecoverStep() {
        super("RecoverStep");
    }

    @Override
    public StepResult execute(CombatAIContext context) {
        // TODO Implement the decision and its actions.
        return StepResult.skip();
    }
}
