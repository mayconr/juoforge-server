package com.github.mayconr.juoserver.game.item.flow.creation.placement;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.OrphanItemTarget;
import com.github.mayconr.juoserver.game.model.event.OrphanItemCreated;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class PlaceOrphanItemStep extends AbstractFlowStep<ItemCreationContext> {
    public PlaceOrphanItemStep() {
        super("configure-orphan-item");
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        final var item = context.result();

        if (context.getTarget() instanceof OrphanItemTarget) {
            item.setCurrentLocation(ItemLocation.orphan());
            context.setEvent(new OrphanItemCreated(context.result()));
            return StepResult.success();
        }
        return StepResult.failure("");
    }
}
