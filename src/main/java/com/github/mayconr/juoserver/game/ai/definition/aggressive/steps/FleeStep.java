package com.github.mayconr.juoserver.game.ai.definition.aggressive.steps;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Placeholder: Cancel combat and move away from threats; stop the flow when handled. */
public final class FleeStep extends AbstractFlowStep<AggressiveAIContext> {
    public FleeStep() {
        super("FleeStep");
    }

    @Override
    public StepResult execute(AggressiveAIContext context) {
        // TODO Implement the decision and its actions.
        return StepResult.skip();
    }
}
