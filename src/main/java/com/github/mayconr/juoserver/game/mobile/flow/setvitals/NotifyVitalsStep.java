package com.github.mayconr.juoserver.game.mobile.flow.setvitals;

import com.github.mayconr.juoserver.game.model.event.MobileVitalsSet;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

final class NotifyVitalsStep extends AbstractFlowStep<SetVitalsContext> {
    private final EventBus eventBus;

    NotifyVitalsStep(EventBus eventBus) {
        super("notify-vitals");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(SetVitalsContext context) {
        eventBus.publish(new MobileVitalsSet(
                context.getMobile(), context.getType(), context.getMax(), context.getValue()
        ));
        return StepResult.success();
    }
}
