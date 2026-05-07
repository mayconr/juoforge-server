package com.github.mayconr.juoserver.game.npc.flow.creation.notification;

import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.model.event.NpcCreated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyNpcCreation extends AbstractFlowStep<NpcCreationContext> {

    private final EventBus eventBus;

    public NotifyNpcCreation(EventBus eventBus) {
        super("NotifyCreation");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        eventBus.publish(new NpcCreated(context.getNpc()));
        return StepResult.success();
    }
}
