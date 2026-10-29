package com.github.mayconr.juoserver.game.item.flow.creation.validation;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

/** Resolves the backpack owner and additional weight for capacity validation. */
public final class PrepareCreatedItemBackpackWeightStep extends AbstractFlowStep<ItemCreationContext> {
    private final RealmStorage storage;

    public PrepareCreatedItemBackpackWeightStep(RealmStorage storage) {
        super("prepare-created-item-backpack-weight");
        this.storage = storage;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        context.setBackpackOwner(null);
        context.setAddedWeight(0);
        if (!(context.getTarget() instanceof ContainerItemTarget target)
                || !(target.container() instanceof UOContainer container)
                || !(container.getCurrentLocation() instanceof EquippedLocation location)) {
            return StepResult.success();
        }
        var mobile = storage.getMobile(location.ownerSerialId()).orElse(null);
        if (mobile == null) {
            return StepResult.failure("MOBILE_NOT_FOUND", "Container owner is not loaded: " + location.ownerSerialId());
        }
        if (!Integer.valueOf(container.getSerialId()).equals(mobile.getBackpack())) {
            return StepResult.success();
        }
        int addedWeight = Math.multiplyExact(context.getData().getUnitWeight(), context.getData().getAmount());
        context.setBackpackOwner(mobile);
        context.setAddedWeight(addedWeight);
        return StepResult.success();
    }

}
