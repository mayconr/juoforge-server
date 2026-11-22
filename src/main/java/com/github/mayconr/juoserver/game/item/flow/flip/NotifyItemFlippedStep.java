package com.github.mayconr.juoserver.game.item.flow.flip;

import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Sends the new model to the clients: object info on the ground, or the item again inside its container. */
final class NotifyItemFlippedStep extends AbstractFlowStep<FlipItemContext> {
    private final EventBus events;

    NotifyItemFlippedStep(EventBus events) {
        super("notify-item-flipped");
        this.events = events;
    }

    @Override
    public StepResult execute(FlipItemContext context) {
        events.publish(new ItemUpdated(context.getItem(), context.getContainer()));
        return StepResult.success();
    }
}
