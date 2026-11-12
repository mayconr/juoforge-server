package com.github.mayconr.juoserver.game.container.flow.add;

import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.Optional;

/**
 * Places an item in a container. A stackable item added to an equipped container joins an existing stack of
 * the same item when stacking is requested; otherwise it is placed as a new item in the container.
 */
public final class PlaceContainerItemStep extends AbstractFlowStep<AddContainerItemContext> {
    private final WorldStorage storage;

    public PlaceContainerItemStep(WorldStorage storage) {
        super("add-item-to-container");
        this.storage = storage;
    }

    @Override
    public StepResult execute(AddContainerItemContext context) {
        var container = context.getContainer();
        var item = context.getItem();

        if (!acceptsItems(container.getCurrentLocation())) {
            return unsupportedLocation(container);
        }

        var stack = shouldStack(context) ? findStack(container, item) : Optional.<UOItem>empty();
        if (stack.isPresent()) {
            joinStack(context, stack.get(), item);
        } else {
            place(context, container, item);
        }
        return StepResult.success();
    }

    /** Containers on the ground, equipped or inside another container accept items; orphan ones do not. */
    private static boolean acceptsItems(ItemLocation location) {
        return switch (location) {
            case GroundLocation ground -> true;
            case EquippedLocation equipped -> true;
            case ContainerLocation inside -> true;
            case OrphanLocation orphan -> false;
        };
    }

    private static StepResult unsupportedLocation(UOContainer container) {
        return StepResult.failure(
                "ITEM_CONTAINER_LOCATION_UNSUPPORTED",
                "Cannot place item in container: unsupported location " + container.getCurrentLocation()
        );
    }

    /** Stacking is requested, the item is stackable and the container is equipped. */
    private static boolean shouldStack(AddContainerItemContext context) {
        return context.isTryStack()
                && context.getItem().hasFlag(ItemFlag.STACKABLE)
                && context.getContainer().getCurrentLocation() instanceof EquippedLocation;
    }

    /** First other item in the container with the same name as the added item. */
    private Optional<UOItem> findStack(UOContainer container, UOItem item) {
        return container.getContainerItems().stream()
                .map(this::containerItem)
                .filter(existing -> existing != item)
                .filter(existing -> existing.getName().equals(item.getName()))
                .findFirst();
    }

    private UOItem containerItem(int serial) {
        return storage.items().get(serial)
                .orElseThrow(() -> new IllegalStateException("Container item not found: " + serial));
    }

    private static void joinStack(AddContainerItemContext context, UOItem stack, UOItem item) {
        stack.absorbStack(item);
        context.complete(stack);
    }

    private static void place(AddContainerItemContext context, UOContainer container, UOItem item) {
        item.setCurrentLocation(ItemLocation.container(container.getSerialId()));
        container.addItemToContainer(item);
        context.complete(item);
    }
}
