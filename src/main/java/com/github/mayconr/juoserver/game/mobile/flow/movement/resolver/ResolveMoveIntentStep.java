package com.github.mayconr.juoserver.game.mobile.flow.movement.resolver;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveMoveIntentStep extends AbstractFlowStep<MovementContext> {
    public ResolveMoveIntentStep() {
        super("ResolveMovementIntent");
    }

    @Override
    public StepResult execute(MovementContext context) {
        final var mobile = context.getMobile();

        if (mobile.getDirection().equals(context.getDirection())) {
            context.setMoveIntent(MovementContext.MoveIntent.FORWARD);
        } else {
            context.setMoveIntent(MovementContext.MoveIntent.TURN);
        }

        return StepResult.success();
    }
}
