package com.github.mayconr.juoserver.game.mobile.flow.movement.validation;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateTargetTileStep extends AbstractFlowStep<MovementContext> {

    private final UOFileReader fileReader;

    public ValidateTargetTileStep(UOFileReader fileReader) {
        super("ValidateTargetTile");
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(MovementContext context) {
        final var mobile = context.getMobile();
        final var targetLocation = context.getTargetLocation();

        if (fileReader.hasBlockingCollision(mobile, targetLocation)) {
            return StepResult.failure("Target location has impassable statics");
        }

        return StepResult.success();
    }
}
