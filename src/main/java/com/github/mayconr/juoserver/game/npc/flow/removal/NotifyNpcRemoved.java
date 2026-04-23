package com.github.mayconr.juoserver.game.npc.flow.removal;

import com.github.mayconr.juoserver.game.flow.NpcRemovalFlowDefinition.NpcRemovalContext;
import com.github.mayconr.juoserver.game.model.event.NpcRemoved;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyNpcRemoved extends AbstractFlowStep<NpcRemovalContext> {

    private final EventBus eventBus;

    public NotifyNpcRemoved(int order, EventBus eventBus) {
        super("NotifyNpcRemoved", order, FlowPhase.CORE);
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(NpcRemovalContext context) {
        eventBus.publish(new NpcRemoved(context.getNpc()));
        return StepResult.success();
    }
}
