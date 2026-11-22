package com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit;

import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class EnqueuePursuitPathMovementStep extends AbstractFlowStep<AggressiveAIContext> {
    public EnqueuePursuitPathMovementStep() {
        super("EnqueuePursuitPathMovementStep");
    }

    @Override
    public StepResult execute(AggressiveAIContext context) {
        var path = context.getPursuit().getPath();
        if (path != null && !path.directions().isEmpty()) {
            context.enqueueAction(new WalkAction(context.npc(), path.directions().getFirst()));
        }
        return StepResult.stop();
    }
}
