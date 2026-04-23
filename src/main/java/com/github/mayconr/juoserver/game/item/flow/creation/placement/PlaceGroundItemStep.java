package com.github.mayconr.juoserver.game.item.flow.creation.placement;

import com.github.mayconr.juoserver.game.flow.ItemCreationFlowDefinition.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.GroundItemTarget;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.event.GroundedItemCreated;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class PlaceGroundItemStep extends AbstractFlowStep<ItemCreationContext> {

    private final RealmStorage storage;

    public PlaceGroundItemStep(RealmStorage storage) {
        super("place-ground-item");
        this.storage = storage;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        if (context.getTarget() instanceof GroundItemTarget(Location location)) {
            final var item = context.result();

            item.setCurrentLocation(new GroundLocation());
            item.setLocation(location);
            storage.placeOnTheGround(item);

            context.setEvent(new GroundedItemCreated(item));
            return StepResult.success();
        }
        return StepResult.failure("");
    }
}
