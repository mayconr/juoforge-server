package com.github.mayconr.juoserver.game.mobile.flow.movement.validation;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext.MoveIntent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class ValidateMobileCollisionStep extends AbstractFlowStep<MovementContext> {

    private final RealmStorage storage;

    public ValidateMobileCollisionStep(RealmStorage storage) {
        super("ValidateMobileCollisionStep");
        this.storage = storage;
    }

    @Override
    public StepResult execute(MovementContext context) {
        // do not check anything when is turning
        if (MoveIntent.TURN.equals(context.getMoveIntent())) {
            return StepResult.success();
        }

        var mobiles = storage.getMobilesAtLocation(context.getTargetLocation());
        if (mobiles.isEmpty()) {
            return StepResult.success();
        }
        return StepResult.failure("There are mobiles at target location");
    }
}
