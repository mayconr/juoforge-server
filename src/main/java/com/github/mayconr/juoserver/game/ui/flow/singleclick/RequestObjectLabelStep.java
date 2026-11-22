package com.github.mayconr.juoserver.game.ui.flow.singleclick;

import com.github.mayconr.juoserver.game.model.event.ObjectLabelRequested;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class RequestObjectLabelStep extends AbstractFlowStep<SingleClickContext> {
    private final EventBus events;

    public RequestObjectLabelStep(EventBus events) {
        super("request-object-label");
        this.events = events;
    }

    @Override
    public StepResult execute(SingleClickContext context) {
        if (context.getObject() != null) {
            events.publish(new ObjectLabelRequested(context.getPlayer(), context.getObject(), context.getMessageStyle()));
        }
        return StepResult.success();
    }
}
