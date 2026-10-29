package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedInContainer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class DropItemContainerStep extends AbstractFlowStep<DropItemContext> {

    private final EventBus eventBus;
    private final WorldContainer containers;

    public DropItemContainerStep(EventBus eventBus, WorldContainer containers) {
        super("drop-item-container");
        this.eventBus = eventBus;
        this.containers = containers;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        final var player = context.getPlayer();
        final var container = context.getTargetContainer().container();
        final var item = context.getItem();
        final var location = context.getTargetContainer().location();

        item.setLocation(location);
        // Dropping into empty container space preserves the requested position and stack.
        // Dropping onto a specific stack is handled by StackItemStep.
        containers.addItem(container, item, false);

        eventBus.publish(new ItemDroppedInContainer(player, container, item, location));

        return StepResult.success();
    }
}
