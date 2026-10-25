package com.github.mayconr.juoserver.game.economy.flow.purchase;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class DeliverPurchaseStep extends AbstractFlowStep<VendorPurchaseContext> {
    private final WorldItem items;

    public DeliverPurchaseStep(WorldItem items) {
        super("deliver-vendor-purchase");
        this.items = items;
    }

    @Override
    public StepResult execute(VendorPurchaseContext context) {
        var target = ContainerItemTarget.of(context.getBackpack(), options -> options.tryStack(false));
        for (var purchase : context.getPurchase().items()) {
            boolean stackable = purchase.template().flags().contains(ItemFlag.STACKABLE);
            int count = stackable ? 1 : purchase.amount();
            int amount = stackable ? purchase.amount() : 1;
            for (int i = 0; i < count; i++) {
                context.getItems().add(items.createItem(
                        ItemRequest.byTemplate(purchase.template()).withAmount(amount), target));
            }
        }
        return StepResult.success();
    }
}

