package com.github.mayconr.juoserver.game.mobile.flow.movement.validation;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateSequenceStep extends AbstractFlowStep<MovementContext> {

    public ValidateSequenceStep() {
        super("ValidateSequenceStep");
    }

    @Override
    public StepResult execute(MovementContext context) {
        if (context.getMobile() instanceof UOPlayer player) {
            final int clientSequence = context.getMoveRequest().getSequence();

            final int expectedSequence = player.movementSequence();

            if (clientSequence != expectedSequence) {
                return StepResult.failure("Invalid sequence value. Expected " + expectedSequence + " but got " + clientSequence);
            }

            return StepResult.success();
        }

        return StepResult.failure("Invalid sequence value");
    }
}
