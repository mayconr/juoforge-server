package com.github.mayconr.juoserver.game.economy.flow.begin;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class AssignVendorSessionStep extends AbstractFlowStep<BeginVendorPurchaseContext> {
    public AssignVendorSessionStep() {
        super("assign-vendor-session");
    }

    @Override
    public StepResult execute(BeginVendorPurchaseContext context) {
        context.getPlayer().setVendorSession(context.getSession());
        return StepResult.success();
    }
}

