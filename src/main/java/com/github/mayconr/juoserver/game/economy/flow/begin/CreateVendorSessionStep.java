package com.github.mayconr.juoserver.game.economy.flow.begin;

import com.github.mayconr.juoserver.game.economy.VendorHandler;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class CreateVendorSessionStep extends AbstractFlowStep<BeginVendorPurchaseContext> {
    private final VendorHandler vendorHandler;

    public CreateVendorSessionStep(VendorHandler vendorHandler) {
        super("create-vendor-session");
        this.vendorHandler = vendorHandler;
    }

    @Override
    public StepResult execute(BeginVendorPurchaseContext context) {
        context.setSession(vendorHandler.createVendorSession(context.getItems()));
        return StepResult.success();
    }
}

