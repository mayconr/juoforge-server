package com.github.mayconr.juoserver.game.container.flow.add;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class PlaceContainerItemStep extends AbstractFlowStep<AddContainerItemContext> {
    private final RealmStorage storage;

    public PlaceContainerItemStep(RealmStorage storage) {
        super("add-item-to-container");
        this.storage = storage;
    }

    @Override
    public StepResult execute(AddContainerItemContext context) {
        var container = context.getContainer();
        var item = context.getItem();
        boolean tryStack = context.isTryStack();
        if (!(container.getCurrentLocation() instanceof GroundLocation)
                && !(container.getCurrentLocation() instanceof EquippedLocation)
                && !(container.getCurrentLocation() instanceof ContainerLocation)) {
            return StepResult.failure("ITEM_CONTAINER_LOCATION_UNSUPPORTED",
                    "Cannot place item in container: unsupported location " + container.getCurrentLocation());
        }
        if (container.getCurrentLocation() instanceof EquippedLocation
                && item.hasFlag(ItemFlag.STACKABLE) && tryStack) {
            for (var serial : container.getContainerItems()) {
                var existing = storage.getItem(serial)
                        .orElseThrow(() -> new IllegalStateException("Container item not found: " + serial));
                if (existing != item && existing.getName().equals(item.getName())) {
                    existing.increaseAmount(item.getAmount());
                    context.complete(existing);
                    return StepResult.success();
                }
            }
        }
        item.setCurrentLocation(ItemLocation.container(container.getSerialId()));
        container.addItemToContainer(item);
        context.complete(item);
        return StepResult.success();
    }
}
