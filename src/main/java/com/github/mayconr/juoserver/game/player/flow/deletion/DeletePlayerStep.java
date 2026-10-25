package com.github.mayconr.juoserver.game.player.flow.deletion;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class DeletePlayerStep extends AbstractFlowStep<DeletePlayerContext> {
    private final RealmStorage storage;

    public DeletePlayerStep(RealmStorage storage) {
        super("delete-player");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DeletePlayerContext context) {
        // The context future tracks persistence completion without blocking the caller.
        storage.deleteMobile(context.getSerialId()).whenComplete((unused, error) -> {
            if (error != null) {
                context.fail(error);
            } else {
                context.complete(null);
            }
        });
        return StepResult.success();
    }
}

