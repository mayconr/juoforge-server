package com.github.mayconr.juoserver.game.item.hold;

import com.github.mayconr.juoserver.game.item.ItemPlacement;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.function.Consumer;

/** Returns a lifted item to its saved origin, or to the player when that origin no longer exists. */
public final class RestoreHeldItem {
    private final WorldStorage storage;
    private final Consumer<UOItem> recordDrop;
    private final WorldMobile mobiles;

    public RestoreHeldItem(WorldStorage storage, Consumer<UOItem> recordDrop, WorldMobile mobiles) {
        this.storage = storage;
        this.recordDrop = recordDrop;
        this.mobiles = mobiles;
    }

    public void restore(HeldItem held, UOPlayer player) {
        var item = held.item();
        ItemPlacement.detach(item, storage, mobiles, false);
        item.setLocation(held.position());
        if (!restoreOrigin(held)) {
            ItemPlacement.placeOnGround(storage, item, player);
            recordDrop.accept(item);
        }
    }

    private boolean restoreOrigin(HeldItem held) {
        var item = held.item();
        return switch (held.origin()) {
            case GroundLocation ground -> {
                ItemPlacement.placeOnGround(storage, item, held.position());
                yield true;
            }
            case ContainerLocation inside -> storage.items().getContainer(inside.containerSerialId())
                    .filter(container -> !container.isOrphan())
                    .map(container -> {
                        ItemPlacement.placeInContainer(container, item);
                        return true;
                    }).orElse(false);
            case EquippedLocation equipped -> storage.mobiles().get(equipped.ownerSerialId())
                    .filter(owner -> owner.isLayerAvailable(item.getLayer()))
                    .map(owner -> {
                        ItemPlacement.equip(owner, item, mobiles);
                        return true;
                    }).orElse(false);
            case OrphanLocation orphan -> false;
        };
    }
}
