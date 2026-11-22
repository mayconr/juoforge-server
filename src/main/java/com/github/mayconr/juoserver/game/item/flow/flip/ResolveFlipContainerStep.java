package com.github.mayconr.juoserver.game.item.flow.flip;

import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Clients refresh an item inside a container through that container. */
final class ResolveFlipContainerStep extends AbstractFlowStep<FlipItemContext> {
    private final WorldStorage storage;

    ResolveFlipContainerStep(WorldStorage storage) {
        super("resolve-flip-container");
        this.storage = storage;
    }

    @Override
    public StepResult execute(FlipItemContext context) {
        if (!(context.getItem().getCurrentLocation() instanceof ContainerLocation inside)) {
            return StepResult.success();
        }
        return storage.items().getContainer(inside.containerSerialId())
                .map(container -> {
                    context.resolveContainer(container);
                    return StepResult.success();
                })
                .orElseGet(() -> StepResult.failure("ITEM_FLIP_CONTAINER", "Container of the item was not found"));
    }
}
