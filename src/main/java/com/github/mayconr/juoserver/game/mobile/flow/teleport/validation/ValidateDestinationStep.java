package com.github.mayconr.juoserver.game.mobile.flow.teleport.validation;

import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportContext;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateDestinationStep extends AbstractFlowStep<TeleportContext> {

    private final UOFileReader fileReader;

    public ValidateDestinationStep(UOFileReader fileReader) {
        super("ValidateDestination");
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(TeleportContext context) {
        final var mobile = context.getMobile();
        final var location = context.getLocation();

        if (fileReader.hasBlockingCollision(mobile, location)) {
            return StepResult.failure("Location has blocking statics");
        }

        return StepResult.success();
    }
}
