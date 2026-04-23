package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedOnTheGround;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class DropItemGroundStep extends AbstractFlowStep<DropItemContext> {

    private final RealmStorage storage;
    private final EventBus eventBus;

    public DropItemGroundStep(RealmStorage storage,  EventBus eventBus) {
        super("drop-item-ground");
        this.storage = storage;
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        final var player = context.getPlayer();
        final var item = context.getItem();
        final var location = context.getTargetGround().location();

        item.setCurrentLocation(new GroundLocation());
        item.setLocation(location);
        storage.placeOnTheGround(item);
        eventBus.publish(new ItemDroppedOnTheGround(player, item, location));

        return StepResult.success();
    }
}
