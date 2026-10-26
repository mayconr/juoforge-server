package com.github.mayconr.juoserver.game.mobile.flow.resurrection;

import com.github.mayconr.juoserver.game.model.event.MobileResurrectEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyResurrectionStep extends AbstractFlowStep<ResurrectionContext> {
    private final EventBus events;

    public NotifyResurrectionStep(EventBus events) {
        super("notify-resurrection");
        this.events = events;
    }

    @Override
    public StepResult execute(ResurrectionContext context) {
        events.publish(new MobileResurrectEvent(context.getMobile()));
        return StepResult.success();
    }
}

