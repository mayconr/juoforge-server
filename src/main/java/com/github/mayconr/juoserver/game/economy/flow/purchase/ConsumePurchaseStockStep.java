package com.github.mayconr.juoserver.game.economy.flow.purchase;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ConsumePurchaseStockStep extends AbstractFlowStep<VendorPurchaseContext> {
    public ConsumePurchaseStockStep() {
        super("consume-vendor-stock");
    }

    @Override
    public StepResult execute(VendorPurchaseContext context) {
        for (var purchase : context.getRequest().getItems()) {
            context.getSession().items().get(purchase.serialId()).entry()
                    .increaseConsumption(purchase.amount());
        }
        return StepResult.success();
    }
}

