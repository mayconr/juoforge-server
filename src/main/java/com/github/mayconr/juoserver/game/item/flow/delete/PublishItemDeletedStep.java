package com.github.mayconr.juoserver.game.item.flow.delete;

import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Publishes the deletion so clients that see the item remove it. */
public final class PublishItemDeletedStep extends AbstractFlowStep<DeleteItemContext> {
    private final EventBus events;

    public PublishItemDeletedStep(EventBus events) {
        super("publish-item-deleted");
        this.events = events;
    }

    @Override
    public StepResult execute(DeleteItemContext context) {
        events.publish(new ItemDeleted(context.getItem()));
        return StepResult.success();
    }
}
