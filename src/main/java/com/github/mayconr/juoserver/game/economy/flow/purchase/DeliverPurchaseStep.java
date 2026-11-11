package com.github.mayconr.juoserver.game.economy.flow.purchase;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Delivers each purchase as one stack when the client tiledata marks its graphic as stackable, else unit by unit. */
public class DeliverPurchaseStep extends AbstractFlowStep<VendorPurchaseContext> {
    private final WorldItem items;
    private final UOFileReader fileReader;

    public DeliverPurchaseStep(WorldItem items, UOFileReader fileReader) {
        super("deliver-vendor-purchase");
        this.items = items;
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(VendorPurchaseContext context) {
        var target = ContainerItemTarget.of(context.getBackpack(), options -> options.tryStack(false));
        for (var purchase : context.getPurchase().items()) {
            boolean stackable = isStackable(purchase.template());
            int count = stackable ? 1 : purchase.amount();
            int amount = stackable ? purchase.amount() : 1;
            for (int i = 0; i < count; i++) {
                context.getItems().add(items.createItem(
                        ItemRequest.byTemplate(purchase.template()).withAmount(amount), target));
            }
        }
        return StepResult.success();
    }

    private boolean isStackable(ItemTemplate template) {
        return fileReader != null && fileReader.isStackable(template.modelId());
    }
}
