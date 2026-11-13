package com.github.mayconr.juoserver.game.item.flow.decay;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.event.GroundedItemCreated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.List;
import java.util.LinkedHashMap;

/** Spills direct contents before deleting their container through the existing item deletion flow. */
public final class ApplyDecayStep extends AbstractFlowStep<DecayItemContext> {
    private final WorldStorage storage;
    private final ItemModule items;
    private final EventBus events;
    private final ValidateDecayItemStep validation;

    public ApplyDecayStep(WorldStorage storage, ItemModule items, EventBus events, ValidateDecayItemStep validation) {
        super("apply-item-decay");
        this.storage = storage;
        this.items = items;
        this.events = events;
        this.validation = validation;
    }

    @Override
    public StepResult execute(DecayItemContext context) {
        // The same monitor is used by lift registration, preventing a new hold during removal.
        synchronized (items.heldItems()) {
            var item = context.getItem();
            if (!validation.canDecay(item)) {
                return StepResult.stop();
            }
            var contents = directContents(context);
            if (contents.stream().anyMatch(items.heldItems()::isHeld)) {
                return StepResult.stop();
            }
            for (var child : contents) {
                spill((UOContainer) item, child);
            }
            items.deleteItem(item);
            return StepResult.success();
        }
    }

    private List<UOItem> directContents(DecayItemContext context) {
        var contents = new LinkedHashMap<Integer, UOItem>();
        context.getContents().forEach(item -> contents.put(item.getSerialId(), item));
        if (context.getItem() instanceof UOContainer container) {
            for (var serial : container.getContainerItems()) {
                var child = storage.items().get(serial)
                        .orElseThrow(() -> new IllegalStateException("Container item not found: " + serial));
                contents.put(serial, child);
            }
        }
        return contents.values().stream()
                .filter(item -> item.getCurrentLocation() instanceof ContainerLocation inside
                        && inside.containerSerialId() == context.getItem().getSerialId())
                .toList();
    }

    private void spill(UOContainer container, UOItem child) {
        container.removeItemFromContainer(child);
        child.setCurrentLocation(ItemLocation.ground());
        child.setLocation(container);
        items.recordGroundInteraction(child);
        storage.items().placeOnGround(child);
        events.publish(new GroundedItemCreated(child));
    }
}
