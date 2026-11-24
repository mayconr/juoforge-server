package com.github.mayconr.juoserver.game.item.flow.drop.notification;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.event.ItemInventoryChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.Objects;

public final class NotifyDropInventoryStep extends AbstractFlowStep<DropItemContext> {
    private final EventBus events;

    public NotifyDropInventoryStep(EventBus events) {
        super("notify-drop-inventory");
        this.events = events;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        var origin = context.getOrigin();
        var item = context.getItem();
        var destination = context.isStack() ? context.getTargetStack().itemStack().getCurrentLocation()
                : item.getCurrentLocation();

        // The add-container flow already covers the destination of a normal container drop.
        if (context.isContainerDrop() && Objects.equals(origin, destination)) {
            return StepResult.success();
        }
        if (context.isContainerDrop()) {
            destination = null;
        }
        if (origin instanceof ContainerLocation || destination instanceof ContainerLocation) {
            events.publish(new ItemInventoryChanged(item, origin, destination));
        }
        return StepResult.success();
    }
}
