package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.event.ItemStacked;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StackItemStep extends AbstractFlowStep<DropItemContext> {

    private final RealmStorage storage;
    private final EventBus eventBus;

    public StackItemStep(RealmStorage storage, EventBus eventBus) {
        super("stack_item");
        this.storage = storage;
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        final var player = context.getPlayer();
        final var itemStacked = context.getTargetStack().itemStack();
        final var itemDropped = context.getItem();

        itemStacked.increaseAmount(itemDropped.getAmount());

        storage.deleteItem(itemDropped);

        switch (itemStacked.getCurrentLocation()) {
            case GroundLocation location -> {
                log.info("Stacking ground item");
                eventBus.publish(new ItemStacked(player, itemStacked, itemDropped, ItemStacked.StackDestination.GROUND, null));
            }
            case ContainerLocation location -> {
                log.info("Stacking container item");
                var container = storage.getContainer(location.containerSerialId())
                                .orElseThrow(() -> new IllegalStateException("Container not found"));
                eventBus.publish(new ItemStacked(player, itemStacked, itemDropped, ItemStacked.StackDestination.CONTAINER, container));
            }
            default -> {
                return StepResult.failure("Unknown location type " + itemStacked.getCurrentLocation());
            }
        }
        return StepResult.success();
    }
}
