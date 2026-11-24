package com.github.mayconr.juoserver.game.container.flow.add;

import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.event.ItemInventoryChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class NotifyContainerInventoryStep extends AbstractFlowStep<AddContainerItemContext> {
    private final EventBus events;

    public NotifyContainerInventoryStep(EventBus events) {
        super("notify-container-inventory");
        this.events = events;
    }

    @Override
    public StepResult execute(AddContainerItemContext context) {
        var destination = ItemLocation.container(context.getContainer().getSerialId());
        events.publish(new ItemInventoryChanged(context.result(), null, destination));
        return StepResult.success();
    }
}
