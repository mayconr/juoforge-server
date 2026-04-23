package com.github.mayconr.juoserver.game.mobile.flow.death;

import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyDeathStep extends AbstractFlowStep<DeathFlowDefinition.DeathContext> {

    private final EventBus eventBus;

    public NotifyDeathStep(EventBus eventBus) {
        super("notify_death_step", 600, FlowPhase.CORE);
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(DeathFlowDefinition.DeathContext context) {
        eventBus.publish(new MobileDeathEvent(context.getKiller(), context.getVictim(), context.getCause(), context.getCorpse()));
        return StepResult.success();
    }
}
