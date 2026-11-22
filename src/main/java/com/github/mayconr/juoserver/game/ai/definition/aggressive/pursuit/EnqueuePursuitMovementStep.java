package com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit;

import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.CombatAIState;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class EnqueuePursuitMovementStep extends AbstractFlowStep<AggressiveAIContext> {
    public EnqueuePursuitMovementStep() {
        super("EnqueuePursuitMovementStep");
    }

    @Override
    public StepResult execute(AggressiveAIContext context) {
        if (context.getState() != CombatAIState.PURSUING || context.getTarget() == null) {
            return StepResult.skip();
        }

        var direction = context.getPursuit().getNextDirection();
        if (direction != null) {
            context.enqueueAction(new WalkAction(context.npc(), direction));
        }
        return StepResult.stop();
    }
}
