package com.github.mayconr.juoserver.game.item.flow.delete;

import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Resolves the mobile wearing the detached container for the shared status notification step. */
public final class FindDetachedContainerOwnerStep extends AbstractFlowStep<DeleteItemContext> {
    private final WorldStorage storage;

    public FindDetachedContainerOwnerStep(WorldStorage storage) {
        super("find-detached-container-owner");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DeleteItemContext context) {
        if (context.getContainer().getCurrentLocation() instanceof EquippedLocation location) {
            storage.mobiles().get(location.ownerSerialId()).ifPresent(context::setMobile);
        }
        return StepResult.success();
    }
}
