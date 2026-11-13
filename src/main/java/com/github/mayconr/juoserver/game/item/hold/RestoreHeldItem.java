package com.github.mayconr.juoserver.game.item.hold;

import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.function.Consumer;

/** Returns a lifted item to its saved origin, or to the player when that origin no longer exists. */
public final class RestoreHeldItem {
    private final WorldStorage storage;
    private final Consumer<UOItem> recordDrop;

    public RestoreHeldItem(WorldStorage storage) {
        this(storage, item -> { });
    }

    public RestoreHeldItem(WorldStorage storage, Consumer<UOItem> recordDrop) {
        this.storage = storage;
        this.recordDrop = recordDrop;
    }

    public void restore(HeldItem held, UOPlayer player) {
        var item = held.item();
        detach(item);
        item.setLocation(held.position());
        if (!restoreOrigin(held)) {
            item.setLocation(player);
            item.setCurrentLocation(ItemLocation.ground());
            recordDrop.accept(item);
            storage.items().placeOnGround(item);
        }
    }

    private void detach(UOItem item) {
        switch (item.getCurrentLocation()) {
            case GroundLocation ground -> storage.items().removeFromGround(item);
            case ContainerLocation inside -> storage.items().getContainer(inside.containerSerialId())
                    .ifPresent(container -> container.removeItemFromContainer(item));
            case EquippedLocation equipped -> storage.mobiles().get(equipped.ownerSerialId())
                    .ifPresent(owner -> owner.getEquippedItems().remove(item.getLayer(), item.getSerialId()));
            case OrphanLocation orphan -> { }
        }
    }

    private boolean restoreOrigin(HeldItem held) {
        var item = held.item();
        return switch (held.origin()) {
            case GroundLocation ground -> {
                item.setCurrentLocation(held.origin());
                storage.items().placeOnGround(item);
                yield true;
            }
            case ContainerLocation inside -> storage.items().getContainer(inside.containerSerialId())
                    .filter(container -> !container.isOrphan())
                    .map(container -> {
                        item.setCurrentLocation(held.origin());
                        container.addItemToContainer(item);
                        return true;
                    }).orElse(false);
            case EquippedLocation equipped -> storage.mobiles().get(equipped.ownerSerialId())
                    .filter(owner -> owner.isLayerAvailable(item.getLayer()))
                    .map(owner -> {
                        item.setCurrentLocation(held.origin());
                        owner.equipItem(item);
                        return true;
                    }).orElse(false);
            case OrphanLocation orphan -> false;
        };
    }
}
