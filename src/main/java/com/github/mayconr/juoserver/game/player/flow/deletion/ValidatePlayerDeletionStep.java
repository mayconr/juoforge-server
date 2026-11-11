package com.github.mayconr.juoserver.game.player.flow.deletion;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class ValidatePlayerDeletionStep extends AbstractFlowStep<DeletePlayerContext> {
    private final WorldStorage storage;

    public ValidatePlayerDeletionStep(WorldStorage storage) {
        super("validate-player-deletion");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DeletePlayerContext context) {
        int serial = context.getSerialId();
        if (serial <= 0 || !UOMobile.isMobile(serial)) {
            return context.fail(new IllegalArgumentException("Invalid player serial: " + serial));
        }
        if (storage.getMobile(serial).isPresent()) {
            return context.fail(new IllegalStateException("Cannot delete a loaded mobile: " + serial));
        }
        return StepResult.success();
    }
}

