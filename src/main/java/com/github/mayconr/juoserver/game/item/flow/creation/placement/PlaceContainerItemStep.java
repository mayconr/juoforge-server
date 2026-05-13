package com.github.mayconr.juoserver.game.item.flow.creation.placement;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget.Options;
import com.github.mayconr.juoserver.game.model.event.ItemCreatedInContainer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class PlaceContainerItemStep extends AbstractFlowStep<ItemCreationContext> {

    private final RealmStorage storage;

    public PlaceContainerItemStep(RealmStorage storage) {
        super("place-container-step");
        this.storage = storage;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        if (context.getTarget() instanceof ContainerItemTarget(UOContainer container, Options options)) {
            // TODO container on the ground

            if (container.getCurrentLocation() instanceof EquippedLocation(Integer ownerSerialId)) {
                final var item = context.result();
                var updatedItem = item;
                var ownerMobile = storage.getMobile(ownerSerialId);

                // search for another similar and try to stack
                if (item.hasFlag(ItemFlag.STACKABLE) && options.tryStack()) {
                    for (Integer itemSerial : container.getContainerItems()) {
                        var containerItem = storage.getItem(itemSerial)
                                .orElseThrow(()->new IllegalStateException("Container item not found"));

                        if (containerItem.getName().equals(item.getName())) {
                            containerItem.increaseAmount(item.getAmount());
                            updatedItem = containerItem;
                            break;
                        }
                    }
                }

                // Item is stackable, but is unable to stack. Should bed added to container
                if (updatedItem.equals(item)) {
                    item.setCurrentLocation(ItemLocation.container(container.getSerialId()));
                    container.addItemToContainer(updatedItem);
                }

                context.setEvent(new ItemCreatedInContainer(container, updatedItem, ownerMobile.orElse(null)));
                return StepResult.success();
            }
        }

        return StepResult.failure("");
    }
}
