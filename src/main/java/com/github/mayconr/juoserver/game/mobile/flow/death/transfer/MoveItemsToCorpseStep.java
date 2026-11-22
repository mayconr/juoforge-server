package com.github.mayconr.juoserver.game.mobile.flow.death.transfer;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemPlacement;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Objects;

public class MoveItemsToCorpseStep extends AbstractFlowStep<DeathContext> {

    private static final EnumSet<Layer> RETAINED_LAYERS = EnumSet.of(
            Layer.BACKPACK, Layer.HAIR, Layer.FACIAL_HAIR,
            Layer.SHOP_BUY, Layer.SHOP_SELL, Layer.SHOP_BUY_RESTOCK
    );

    private final MobileModule mobileModule;
    private final ItemModule itemModule;
    private final WorldStorage storage;

    public MoveItemsToCorpseStep(MobileModule mobileModule, ItemModule itemModule, WorldStorage storage) {
        super("move_item_corpse_step");
        this.mobileModule = mobileModule;
        this.itemModule = itemModule;
        this.storage = storage;
    }

    @Override
    public StepResult execute(DeathContext context) {
        var serial = context.getVictim().getEquippedItems().get(Layer.BACKPACK);
        if (serial == null) {
            return transfer(context, null);
        }

        var backpack = storage.items().get(serial)
                .filter(UOContainer.class::isInstance)
                .map(UOContainer.class::cast)
                .orElseThrow(() -> new IllegalStateException("Backpack not found: " + serial));

        // The player may die before ever opening the backpack: load its direct children first.
        return StepResult.async(storage.items().loadContainerItems(backpack).thenApply(items -> {
            backpack.addItemsToContainer(items);
            return transfer(context, backpack);
        }));
    }

    private StepResult transfer(DeathContext context, UOContainer backpack) {
        var victim = context.getVictim();
        var corpse = Objects.requireNonNull(context.getCorpse(), "Corpse");
        // Collect equipment that leaves the victim and verify its slots.
        var equippedItems = new ArrayList<UOItem>();
        for (var entry : victim.getEquippedItems().entrySet()) {
            if (!RETAINED_LAYERS.contains(entry.getKey())) {
                var item = load(entry.getValue());
                if (item.getLayer() != entry.getKey()
                        || !ItemLocation.equipped(victim.getSerialId()).equals(item.getCurrentLocation())) {
                    throw new IllegalStateException("Invalid equipment origin: " + item.getSerialId());
                }
                equippedItems.add(item);
            }
        }

        // Load direct backpack contents so nested containers move intact.
        var contents = new ArrayList<UOItem>();
        if (backpack != null) {
            for (var serial : backpack.getContainerItems()) {
                var item = load(serial);
                if (!ItemLocation.container(backpack.getSerialId()).equals(item.getCurrentLocation())) {
                    throw new IllegalStateException("Invalid backpack item origin: " + serial);
                }
                contents.add(item);
            }
        }

        // Prepare equipment transfers. Mounts are removed for separate handling.
        var transfers = new ArrayList<Runnable>();
        for (var item : equippedItems) {
            if (item.getLayer() == Layer.MOUNT || item.hasFlag(ItemFlag.MOUNT)) {
                transfers.add(() -> {
                    ItemPlacement.removeEquipment(victim, item, mobileModule);
                    context.setMountItem(item);
                    itemModule.deleteItem(item);
                });
            } else {
                var transfer = prepareTransfer(item, corpse);
                transfers.add(() -> {
                    transfer.run();
                    // Keep the original layer for the corpse's equipment display.
                    corpse.recordEquippedItem(item);
                });
            }
        }

        // Prepare backpack transfers before changing any item links.
        for (var item : contents) {
            transfers.add(prepareTransfer(item, corpse));
        }

        // Apply the prepared transfers once all origins have been checked.
        transfers.forEach(Runnable::run);

        // Refresh the player's gold balance once after emptying the backpack.
        if (backpack != null && victim instanceof UOPlayer) {
            mobileModule.recalculateGold(victim);
        }

        return StepResult.success();
    }

    private Runnable prepareTransfer(UOItem item, UOCorpse corpse) {
        return ItemPlacement.prepareContainerMove(
                item,
                corpse,
                new PointInTheWorld(0, 0, 0),
                storage,
                mobileModule,
                () -> ItemPlacement.placeInContainer(corpse, item)
        );
    }

    private UOItem load(int serial) {
        return storage.items().get(serial)
                .orElseThrow(() -> new IllegalStateException("Item not found: " + serial));
    }
}
