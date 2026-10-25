package com.github.mayconr.juoserver.game.economy.flow.begin;

import com.github.mayconr.juoserver.game.economy.VendorHandler;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class BeginVendorPurchaseFlowDefinition {
    private BeginVendorPurchaseFlowDefinition() {}

    public static Flow<BeginVendorPurchaseContext> build(VendorHandler vendorHandler, EventBus events) {
        return FlowFactory.<BeginVendorPurchaseContext>builder()
                .step(new CreateVendorSessionStep(vendorHandler))
                .step(new AssignVendorSessionStep())
                .step(new NotifyVendorSessionOpenedStep(events))
                .build();
    }
}

