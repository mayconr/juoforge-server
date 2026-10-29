package com.github.mayconr.juoserver.game.economy.flow.purchase;

import com.github.mayconr.juoserver.game.economy.VendorHandler;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolvePurchaseStep extends AbstractFlowStep<VendorPurchaseContext> {
    private final VendorHandler vendorHandler;
    private final Wallet wallet;
    private final WorldContainer containers;

    public ResolvePurchaseStep(VendorHandler vendorHandler, Wallet wallet, WorldContainer containers) {
        super("resolve-vendor-purchase");
        this.vendorHandler = vendorHandler;
        this.wallet = wallet;
        this.containers = containers;
    }

    @Override
    public StepResult execute(VendorPurchaseContext context) {
        var backpack = containers.getContainerBySerialId(context.getPlayer().getBackpack());
        if (backpack.isEmpty()) return StepResult.failure("Backpack not found");
        context.setBackpack(backpack.get());
        var purchase = vendorHandler.resolveVendorPurchase(context.getPlayer(), context.getRequest(), wallet);
        context.setPurchase(purchase);
        return purchase.success() ? StepResult.success() : StepResult.failure("Purchase failed");
    }
}
