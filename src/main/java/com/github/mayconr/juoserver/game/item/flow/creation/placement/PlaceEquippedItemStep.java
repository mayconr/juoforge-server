package com.github.mayconr.juoserver.game.item.flow.creation.placement;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.model.EquipItemTarget;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.EquippedItemCreated;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class PlaceEquippedItemStep extends AbstractFlowStep<ItemCreationContext> {
    private final ItemModule items;

    public PlaceEquippedItemStep(ItemModule items) {
        super("place-equipped-item");
        this.items = items;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        if (context.getTarget() instanceof EquipItemTarget(UOMobile mobile)) {
            var item = context.result();

            if (!items.equipItem(mobile, item, false)) {
                return StepResult.failure("EQUIP_FAILED", "Created item could not be equipped");
            }
            context.setEvent(new EquippedItemCreated(mobile, item));

            return StepResult.success();
        }

        return StepResult.failure("ITEM_TARGET_MISMATCH",
                "Cannot equip item: expected an equipment target; target=" + context.getTarget());
    }
}
