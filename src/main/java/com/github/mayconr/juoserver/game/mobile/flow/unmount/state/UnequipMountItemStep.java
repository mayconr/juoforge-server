package com.github.mayconr.juoserver.game.mobile.flow.unmount.state;

import com.github.mayconr.juoserver.game.item.ItemPlacement;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.mobile.flow.unmount.UnmountContext;
import com.github.mayconr.juoserver.game.model.event.ItemUnequipped;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class UnequipMountItemStep extends AbstractFlowStep<UnmountContext> {

    private final WorldStorage storage;
    private final EventBus eventBus;
    private final WorldMobile mobiles;

    public UnequipMountItemStep(WorldStorage storage, EventBus eventBus, WorldMobile mobiles) {
        super("UnequipMountItem");
        this.storage = storage;
        this.eventBus = eventBus;
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(UnmountContext context) {
        final var mobile = context.getMobile();
        final var mountItem = context.getMountItem();

        ItemPlacement.removeEquipment(mobile, mountItem, mobiles);
        storage.items().delete(mountItem);

        eventBus.publish(new ItemUnequipped(mobile, mountItem));

        return StepResult.success();
    }
}
