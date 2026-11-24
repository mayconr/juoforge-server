package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.ItemPlacement;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedInContainer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class DropItemContainerStep extends AbstractFlowStep<DropItemContext> {

    private final EventBus eventBus;
    private final WorldContainer containers;

    private final WorldStorage storage;
    private final WorldMobile mobiles;

    public DropItemContainerStep(EventBus eventBus, WorldContainer containers,
                                 WorldStorage storage, WorldMobile mobiles) {
        super("drop-item-container");
        this.eventBus = eventBus;
        this.containers = containers;
        this.storage = storage;
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        final var player = context.getPlayer();
        final var container = context.getTargetContainer().container();
        final var item = context.getItem();
        final var location = context.getTargetContainer().location();

        context.setOrigin(item.getCurrentLocation());
        ItemPlacement.prepareContainerMove(item, container, location, storage, mobiles,
                () -> containers.addItem(container, item, false)).run();

        if (player != null) {
            eventBus.publish(new ItemDroppedInContainer(player, container, item, location));
        }

        return StepResult.success();
    }
}
