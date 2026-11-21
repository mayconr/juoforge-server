package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.ItemPlacement;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedOnTheGround;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class DropItemGroundStep extends AbstractFlowStep<DropItemContext> {

    private final WorldStorage storage;
    private final EventBus eventBus;

    public DropItemGroundStep(WorldStorage storage,  EventBus eventBus) {
        super("drop-item-ground");
        this.storage = storage;
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        final var player = context.getMobile();
        final var item = context.getItem();
        final var location = context.getTargetGround().location();

        ItemPlacement.placeOnGround(storage, item, location);
        eventBus.publish(new ItemDroppedOnTheGround(player, item, location));

        return StepResult.success();
    }
}
