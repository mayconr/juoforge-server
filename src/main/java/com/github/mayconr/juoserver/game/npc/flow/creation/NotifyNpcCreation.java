package com.github.mayconr.juoserver.game.npc.flow.creation;

import com.github.mayconr.juoserver.game.flow.NpcCreationFlowDefinition;
import com.github.mayconr.juoserver.game.model.event.NpcCreated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyNpcCreation extends AbstractFlowStep<NpcCreationFlowDefinition.NpcCreationContext> {

    private final EventBus eventBus;

    public NotifyNpcCreation(int order, EventBus eventBus) {
        super("NotifyCreation", order, FlowPhase.CORE);
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(NpcCreationFlowDefinition.NpcCreationContext context) {
        eventBus.publish(new NpcCreated(context.getNpc()));
        return StepResult.success();
    }
}
