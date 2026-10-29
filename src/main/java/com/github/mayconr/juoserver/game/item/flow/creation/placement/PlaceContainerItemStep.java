package com.github.mayconr.juoserver.game.item.flow.creation.placement;

import com.github.mayconr.juoserver.game.container.WorldContainer;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget.Options;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class PlaceContainerItemStep extends AbstractFlowStep<ItemCreationContext> {

    private final WorldContainer containers;

    public PlaceContainerItemStep(WorldContainer containers) {
        super("place-container-step");
        this.containers = containers;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        if (context.getTarget() instanceof ContainerItemTarget target && target.container() == null) {
            return StepResult.failure("ITEM_CONTAINER_REQUIRED",
                    "Cannot place item in container: target container is null");
        }
        if (context.getTarget() instanceof ContainerItemTarget(UOContainer container, Options options)) {
            if (container.getCurrentLocation() instanceof GroundLocation
                    || container.getCurrentLocation() instanceof EquippedLocation) {
                var item = containers.addItem(container, context.result(), options.tryStack(),
                        context.getOptions().renderOnCreate());
                context.complete(item);
                return StepResult.success();
            }
        }

        return StepResult.failure("ITEM_CONTAINER_LOCATION_UNSUPPORTED",
                "Cannot place item in container: expected a UOContainer on the ground or equipped by a mobile; target="
                        + context.getTarget());
    }
}
