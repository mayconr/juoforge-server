package com.github.mayconr.juoserver.game.item.flow.creation.notification;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyItemCreatedStep extends AbstractFlowStep<ItemCreationContext> {

    private final EventBus eventBus;

    public NotifyItemCreatedStep(EventBus eventBus) {
        super("notify-item-creation-step");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        eventBus.publish(context.getEvent());
        return StepResult.success();
    }
}
