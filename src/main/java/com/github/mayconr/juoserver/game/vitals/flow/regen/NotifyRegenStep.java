package com.github.mayconr.juoserver.game.vitals.flow.regen;

import com.github.mayconr.juoserver.game.model.event.VitalsChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class NotifyRegenStep extends AbstractFlowStep<RegenContext> {
    private final EventBus eventBus;

    public NotifyRegenStep(EventBus eventBus) {
        super("notify-regen");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(RegenContext context) {
        if (context.isChanged()) {
            eventBus.publish(new VitalsChanged(context.getMobile()));
        }
        return StepResult.success();
    }
}
