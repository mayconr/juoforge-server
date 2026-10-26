package com.github.mayconr.juoserver.game.mobile.flow.death.transfer;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class MoveItemsToCorpseStep extends AbstractFlowStep<DeathContext> {
    private static final EnumSet<Layer> RETAINED_LAYERS = EnumSet.of(
            Layer.BACKPACK, Layer.BANK, Layer.HAIR, Layer.FACIAL_HAIR,
            Layer.SHOP_BUY, Layer.SHOP_SELL, Layer.SHOP_BUY_RESTOCK);
    private final MobileModule mobileModule;
    private final ItemModule itemModule;
    private final RealmStorage storage;

    public MoveItemsToCorpseStep(MobileModule mobileModule, ItemModule itemModule, RealmStorage storage) {
        super("move_item_corpse_step");
        this.mobileModule = mobileModule;
        this.itemModule = itemModule;
        this.storage = storage;
    }

    @Override
    public StepResult execute(DeathContext context) {
        var serial = context.getVictim().getEquippedItems().get(Layer.BACKPACK);
        if (serial == null) return transfer(context, null);
        var backpack = storage.getItem(serial).filter(UOContainer.class::isInstance)
                .map(UOContainer.class::cast).orElseThrow(() -> new IllegalStateException("Backpack not found: " + serial));
        // The player may die before ever opening the backpack: load its direct children first.
        return StepResult.async(storage.loadContainerItems(backpack).thenApply(items -> {
            backpack.addItemsToContainer(items);
            return transfer(context, backpack);
        }));
    }

    private StepResult transfer(DeathContext context, UOContainer backpack) {
        var victim = context.getVictim();
        var corpse = Objects.requireNonNull(context.getCorpse(), "Corpse");
        var equipped = new ArrayList<UOItem>();
        for (var entry : victim.getEquippedItems().entrySet()) {
            if (!RETAINED_LAYERS.contains(entry.getKey())) equipped.add(load(entry.getValue()));
        }
        var contents = new ArrayList<UOItem>();
        if (backpack != null) {
            for (var serial : backpack.getContainerItems()) contents.add(load(serial));
        }
        // Resolve all items before mutating ownership. Death does not use manual unequip restrictions.
        for (var item : equipped) {
            victim.unequipItem(item);
            if (item.getLayer() == Layer.MOUNT || item.hasFlag(ItemFlag.MOUNT)) {
                context.setMountItem(item);
                itemModule.deleteItem(item);
            } else {
                placeInCorpse(item, corpse);
                corpse.addEquippedItem(item);
            }
        }
        for (var item : contents) {
            backpack.removeItemFromContainer(item);
            placeInCorpse(item, corpse);
            corpse.addItemToContainer(item);
            // Nested containers move as a unit: their children still reference that container.
        }
        if (backpack != null && victim instanceof UOPlayer) mobileModule.recalculateGold(victim);
        return StepResult.success();
    }

    private UOItem load(int serial) {
        return storage.getItem(serial).orElseThrow(() -> new IllegalStateException("Item not found: " + serial));
    }

    private void placeInCorpse(UOItem item, UOCorpse corpse) {
        item.setCurrentLocation(ItemLocation.container(corpse.getSerialId()));
        item.setX(ThreadLocalRandom.current().nextInt(20, 75));
        item.setY(ThreadLocalRandom.current().nextInt(85, 165));
        item.setZ(0);
    }
}
