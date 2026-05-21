package com.github.mayconr.juoserver.game.mobile.flow.movement.apply;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class UpdateSequenceStep extends AbstractFlowStep<MovementContext> {
    public UpdateSequenceStep() {
        super("UpdateSequenceStep");
    }

    @Override
    public StepResult execute(MovementContext context) {
        if (context.getMobile() instanceof UOPlayer player) {
            final var clientSequence = context.getMoveRequest().getSequence();

            player.movementSequence((player.movementSequence() + 1) & 0xFF);

            context.setSequence(clientSequence);
        }

        return StepResult.success();
    }
}
