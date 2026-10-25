package com.github.mayconr.juoserver.game.economy.flow.purchase;

import com.github.mayconr.juoserver.game.economy.VendorHandler;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class VendorPurchaseFlowDefinition {
    private VendorPurchaseFlowDefinition() {}

    public static Flow<VendorPurchaseContext> build(VendorHandler vendorHandler, Wallet wallet, WorldItem items, EventBus events) {
        return FlowFactory.<VendorPurchaseContext>builder()
                .step(new ResolvePurchaseStep(vendorHandler, wallet, items))
                .step(new DeliverPurchaseStep(items))
                .step(new ConsumePurchaseStockStep())
                .step(new NotifyPurchaseStep(events))
                .build();
    }
}
