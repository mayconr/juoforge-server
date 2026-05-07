package com.github.mayconr.juoserver.game.mobile.flow.unmount.state;

import com.github.mayconr.juoserver.game.mobile.flow.unmount.UnmountContext;
import com.github.mayconr.juoserver.game.model.event.ItemUnequipped;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class UnequipMountItemStep extends AbstractFlowStep<UnmountContext> {

    private final RealmStorage storage;
    private final EventBus  eventBus;

    public UnequipMountItemStep(RealmStorage storage,  EventBus eventBus) {
        super("UnequipMountItem");
        this.storage = storage;
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(UnmountContext context) {
        final var mobile = context.getMobile();
        final var mountItem = context.getMountItem();

        mobile.unequipItem(mountItem);
        storage.deleteItem(mountItem);

        eventBus.publish(new ItemUnequipped(mobile, mountItem));

        return StepResult.success();
    }
}
