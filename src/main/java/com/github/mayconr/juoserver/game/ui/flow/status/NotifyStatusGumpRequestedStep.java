package com.github.mayconr.juoserver.game.ui.flow.status;

import com.github.mayconr.juoserver.game.model.event.StatusGumpRequested;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class NotifyStatusGumpRequestedStep extends AbstractFlowStep<StatusGumpContext> {
    private final EventBus events;

    public NotifyStatusGumpRequestedStep(EventBus events) {
        super("notify-status-gump-requested");
        this.events = events;
    }

    @Override
    public StepResult execute(StatusGumpContext context) {
        events.publish(new StatusGumpRequested(context.getPlayer(), context.getMobile(), context.getWeight(), context.getMaxWeight()));
        return StepResult.success();
    }
}
