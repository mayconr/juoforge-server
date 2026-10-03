package com.github.mayconr.juoserver.game.ai.definition.aggressive.steps;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Placeholder: Wait in safety until recovered; stop the flow when handled. */
public final class RecoverStep extends AbstractFlowStep<AggressiveAIContext> {
    public RecoverStep() {
        super("RecoverStep");
    }

    @Override
    public StepResult execute(AggressiveAIContext context) {
        // TODO Implement the decision and its actions.
        return StepResult.skip();
    }
}
