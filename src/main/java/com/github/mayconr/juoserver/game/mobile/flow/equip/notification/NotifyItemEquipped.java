package com.github.mayconr.juoserver.game.mobile.flow.equip.notification;

import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.game.model.event.ItemEquipped;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyItemEquipped extends AbstractFlowStep<EquipItemContext> {

    private final EventBus eventBus;

    public NotifyItemEquipped(EventBus eventBus) {
        super("notifyItemEquipped");
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
