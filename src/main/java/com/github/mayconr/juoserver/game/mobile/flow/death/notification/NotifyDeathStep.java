package com.github.mayconr.juoserver.game.mobile.flow.death.notification;

import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyDeathStep extends AbstractFlowStep<DeathContext> {

    private final EventBus eventBus;

    public NotifyDeathStep(EventBus eventBus) {
        super("notify_death_step");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(DeathContext context) {
        eventBus.publish(new MobileDeathEvent(context.getKiller(), context.getVictim(), context.getCause(), context.getCorpse()));
        return StepResult.success();
    }
}
