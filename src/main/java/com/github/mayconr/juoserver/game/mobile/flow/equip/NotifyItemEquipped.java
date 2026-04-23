package com.github.mayconr.juoserver.game.mobile.flow.equip;

import com.github.mayconr.juoserver.game.flow.EquipItemFlowDefinition.EquipItemContext;
import com.github.mayconr.juoserver.game.model.event.ItemEquipped;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyItemEquipped extends AbstractFlowStep<EquipItemContext> {

    private final EventBus eventBus;

    public NotifyItemEquipped(int order, EventBus eventBus) {
        super("notify_item_equipped", order, FlowPhase.CORE);
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        final var mobile = context.getMobile();
        final var item = context.getItem();

        eventBus.publish(new ItemEquipped(mobile, item));

        return StepResult.success();
    }
}
