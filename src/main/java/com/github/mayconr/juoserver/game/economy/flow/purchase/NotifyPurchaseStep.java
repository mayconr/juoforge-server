package com.github.mayconr.juoserver.game.economy.flow.purchase;

import com.github.mayconr.juoserver.game.model.event.VendorPurchaseCompleted;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.List;

public class NotifyPurchaseStep extends AbstractFlowStep<VendorPurchaseContext> {
    private final EventBus events;

    public NotifyPurchaseStep(EventBus events) {
        super("notify-vendor-purchase");
        this.events = events;
    }

    @Override
    public StepResult execute(VendorPurchaseContext context) {
        context.getPlayer().setVendorSession(null);
        events.publish(new VendorPurchaseCompleted(context.getPlayer(), List.copyOf(context.getItems())));
        return StepResult.success();
    }
}

