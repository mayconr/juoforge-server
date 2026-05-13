package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedInContainer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class DropItemContainerStep extends AbstractFlowStep<DropItemContext> {

    private final EventBus eventBus;

    public DropItemContainerStep(EventBus eventBus) {
        super("drop-item-container");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        final var player = context.getPlayer();
        final var container = context.getTargetContainer().container();
        final var item = context.getItem();
        final var location = context.getTargetContainer().location();

        item.setCurrentLocation(new ContainerLocation(container.getSerialId()));
        item.setLocation(location);
        container.addItemToContainer(item);

        eventBus.publish(new ItemDroppedInContainer(player, container, item, location));

        return StepResult.success();
    }
}
