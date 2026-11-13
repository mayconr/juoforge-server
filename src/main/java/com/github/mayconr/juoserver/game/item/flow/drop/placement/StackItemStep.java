package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.event.ItemStacked;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class StackItemStep extends AbstractFlowStep<DropItemContext> {

    private final WorldStorage storage;
    private final EventBus eventBus;

    public StackItemStep(WorldStorage storage, EventBus eventBus) {
        super("stack_item");
        this.storage = storage;
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        final var player = context.getPlayer();
        final var itemStacked = context.getTargetStack().itemStack();
        final var itemDropped = context.getItem();

        var event = switch (itemStacked.getCurrentLocation()) {
            case GroundLocation location ->
                    new ItemStacked(player, itemStacked, itemDropped, ItemStacked.StackDestination.GROUND, null);
            case ContainerLocation location -> {
                var container = storage.items().getContainer(location.containerSerialId())
                        .orElseThrow(() -> new IllegalStateException("Container not found"));
                yield new ItemStacked(player, itemStacked, itemDropped, ItemStacked.StackDestination.CONTAINER, container);
            }
            default -> null;
        };
        if (event == null) {
            return StepResult.failure("Unknown location type " + itemStacked.getCurrentLocation());
        }
        absorbAndDelete(context);
        eventBus.publish(event);
        return StepResult.success();
    }

    private void absorbAndDelete(DropItemContext context) {
        var target = context.getTargetStack().itemStack();
        var amount = target.getAmount();
        var material = target.getMaterial();
        var quality = target.getQuality();
        try {
            target.absorbStack(context.getItem());
            storage.items().delete(context.getItem());
        } catch (RuntimeException failure) {
            target.setAmount(amount);
            target.setMaterial(material);
            target.setQuality(quality);
            throw failure;
        }
    }
}
