package com.github.mayconr.juoserver.game.mobile.flow.movement.validation;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateMovementParamsStep extends AbstractFlowStep<MovementContext> {

    public ValidateMovementParamsStep() {
        super("ValidateMovementParamsStep");
    }

    @Override
    public StepResult execute(MovementContext context) {
        if (context.getMobile() == null) {
            return StepResult.failure("Mobile is null");
        }
        if (context.getDirection() == null) {
            return StepResult.failure("Direction is null");
        }

        if (context.isRequested() && context.getMoveRequest() == null) {
            return StepResult.failure("Move request is null");
        }
        return StepResult.success();
    }
}
