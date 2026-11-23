package com.github.mayconr.juoserver.game.item.flow.lift.validation;

import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRoot.CarriedBy;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRoot.OnGround;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRoot.Unreachable;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Finds the {@link ItemRoot} of an item; a nesting deeper than the limit is treated as unreachable. */
public final class ItemRootResolver {
    static final int MAX_DEPTH = 64;

    private final WorldStorage storage;
    private final WorldItem items;

    public ItemRootResolver(WorldStorage storage, WorldItem items) {
        this.storage = storage;
        this.items = items;
    }

    public ItemRoot resolve(UOItem item) {
        var current = item;
        for (int depth = 0; depth < MAX_DEPTH; depth++) {
            if (!(current.getCurrentLocation() instanceof ContainerLocation inside)) {
                return rootAt(current);
            }
            var parent = storage.items().get(inside.containerSerialId());
            if (parent.isEmpty()) {
                return new Unreachable();
            }
            current = parent.get();
        }
        return new Unreachable();
    }

    private ItemRoot rootAt(UOItem outermost) {
        synchronized (outermost) {
            if (Boolean.TRUE.equals(items.getProperty(outermost, WorldItem.REQUIRE_OPEN_ACCESS, false))
                    && !Boolean.TRUE.equals(items.getProperty(outermost, WorldItem.ACCESS_OPEN, true))) {
                return new Unreachable();
            }
        }
        return switch (outermost.getCurrentLocation()) {
            case GroundLocation ground -> new OnGround(outermost);
            case EquippedLocation equipped -> carrier(equipped);
            case OrphanLocation orphan -> new Unreachable();
            case ContainerLocation inside -> new Unreachable();
        };
    }

    private ItemRoot carrier(EquippedLocation equipped) {
        return storage.mobiles().get(equipped.ownerSerialId())
                .<ItemRoot>map(CarriedBy::new)
                .orElseGet(Unreachable::new);
    }
}
