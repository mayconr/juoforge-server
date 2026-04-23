package com.github.mayconr.juoserver.game.item.flow.creation.placement;

import com.github.mayconr.juoserver.game.flow.ItemCreationFlowDefinition.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.EquipItemTarget;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.EquippedItemCreated;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class PlaceEquippedItemStep extends AbstractFlowStep<ItemCreationContext> {
    public PlaceEquippedItemStep() {
        super("place-equipped-item");
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        if (context.getTarget() instanceof EquipItemTarget(UOMobile mobile)) {
            var item = context.result();

            mobile.equipItem(item);
            context.setEvent(new EquippedItemCreated(mobile, item));

            return StepResult.success();
        }

        return StepResult.failure("");
    }
}
