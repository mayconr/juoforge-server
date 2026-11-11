package com.github.mayconr.juoserver.game.item.flow.delete;

import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Removes the item from its loaded parent container, so the container no longer lists it. */
public final class DetachDeletedItemStep extends AbstractFlowStep<DeleteItemContext> {
    private final WorldStorage storage;

    public DetachDeletedItemStep(WorldStorage storage) {
        super("detach-deleted-item");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DeleteItemContext context) {
        var item = context.getItem();
        if (item.getCurrentLocation() instanceof ContainerLocation location) {
            storage.items().getContainer(location.containerSerialId()).ifPresent(container -> {
                container.removeItemFromContainer(item);
                context.setContainer(container);
            });
        }
        return StepResult.success();
    }
}
