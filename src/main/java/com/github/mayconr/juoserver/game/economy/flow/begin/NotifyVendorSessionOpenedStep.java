package com.github.mayconr.juoserver.game.economy.flow.begin;

import com.github.mayconr.juoserver.game.model.event.VendorSessionOpened;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NotifyVendorSessionOpenedStep extends AbstractFlowStep<BeginVendorPurchaseContext> {
    private final EventBus events;

    public NotifyVendorSessionOpenedStep(EventBus events) {
        super("notify-vendor-session-opened");
        this.events = events;
    }

    @Override
    public StepResult execute(BeginVendorPurchaseContext context) {
        events.publish(new VendorSessionOpened(context.getPlayer(), context.getVendor(), context.getSession()));
        return StepResult.success();
    }
}

