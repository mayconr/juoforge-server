package com.github.mayconr.juoserver.game.mobile.flow.equip.state;

import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class CleanupItemStateStep extends AbstractFlowStep<EquipItemContext> {
    private final RealmStorage storage;

    public CleanupItemStateStep(RealmStorage storage) {
        super("cleanup_item_state");
        this.storage = storage;
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        final var mobile = context.getMobile();
        final var item = context.getItem();

        switch (item.getCurrentLocation()) {
            case GroundLocation location -> storage.removeFromTheGround(item);
            case EquippedLocation  location -> storage.getMobile(location.ownerSerialId()).ifPresent(mob->mob.unequipItem(item));
            case ContainerLocation location -> storage.getContainer(location.containerSerialId()).ifPresent(cnt->cnt.removeItemFromContainer(item));
            case OrphanLocation location -> {}
        }

        return StepResult.success();
    }
}
