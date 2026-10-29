package com.github.mayconr.juoserver.game.container.flow.add;

import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.event.ItemCreatedInContainer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class NotifyContainerItemStep extends AbstractFlowStep<AddContainerItemContext> {
    private final RealmStorage storage;
    private final EventBus events;

    public NotifyContainerItemStep(RealmStorage storage, EventBus events) {
        super("notify-container-item-added");
        this.storage = storage;
        this.events = events;
    }

    @Override
    public StepResult execute(AddContainerItemContext context) {
        var container = context.getContainer();
        var owner = container.getCurrentLocation() instanceof EquippedLocation location
                ? storage.getMobile(location.ownerSerialId()).orElse(null) : null;
        events.publish(new ItemCreatedInContainer(container, context.result(), owner));
        return StepResult.success();
    }
}
