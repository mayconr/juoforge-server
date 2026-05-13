package com.github.mayconr.juoserver.game.npc.flow.removal.notification;

import com.github.mayconr.juoserver.game.model.event.NpcRemoved;
import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyNpcRemoved extends AbstractFlowStep<NpcRemovalContext> {

    private final EventBus eventBus;

    public NotifyNpcRemoved(EventBus eventBus) {
        super("NotifyNpcRemoved");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(NpcRemovalContext context) {
        eventBus.publish(new NpcRemoved(context.getNpc()));
        return StepResult.success();
    }
}
