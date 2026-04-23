package com.github.mayconr.juoserver.game.mobile.flow.unequip;

import com.github.mayconr.juoserver.game.flow.UnequipItemFlowDefinition.UnequipItemContext;
import com.github.mayconr.juoserver.game.model.event.ItemUnequipped;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyUnequipItemStep extends AbstractFlowStep<UnequipItemContext> {

    private final EventBus eventBus;

    public NotifyUnequipItemStep(int order,  EventBus eventBus) {
        super("notify_unequip_item", order, FlowPhase.CORE);
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(UnequipItemContext context) {
        eventBus.publish(new ItemUnequipped(context.getMobile(), context.getItem()));
        return StepResult.success();
    }
}
