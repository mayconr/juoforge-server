package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/** Updates item locations and their corresponding world, container and equipment links. */
public final class ItemPlacement {
    private ItemPlacement() {
    }

    public static void detach(UOItem item, WorldStorage storage, WorldMobile mobiles, boolean requireParent) {
        switch (item.getCurrentLocation()) {
            case GroundLocation ground -> storage.items().removeFromGround(item);
            case ContainerLocation inside -> findParent(
                    storage.items().getContainer(inside.containerSerialId()), requireParent, "Container not found"
            ).ifPresent(container -> container.removeItemFromContainer(item));
            case EquippedLocation equipped -> findParent(
                    storage.mobiles().get(equipped.ownerSerialId()), requireParent, "Owner not found"
            ).ifPresent(owner -> removeEquipment(owner, item, mobiles));
            case OrphanLocation orphan -> { }
        }
        item.setCurrentLocation(ItemLocation.orphan());
    }

    private static <T> Optional<T> findParent(Optional<T> parent, boolean required, String failure) {
        if (required && parent.isEmpty()) {
            throw new IllegalStateException(failure);
        }
        return parent;
    }

    public static void removeEquipment(UOMobile owner, UOItem item, WorldMobile mobiles) {
        if (owner.unequipItem(item)) {
            mobiles.removeItemResistances(owner, item);
        }
    }

    public static void equip(UOMobile owner, UOItem item, WorldMobile mobiles) {
        item.setCurrentLocation(ItemLocation.equipped(owner.getSerialId()));
        owner.equipItem(item);
        mobiles.addItemResistances(owner, item);
    }

    public static void placeOnGround(WorldStorage storage, UOItem item, Location destination) {
        var origin = item.getCurrentLocation();
        var position = new PointInTheWorld(item);
        item.setLocation(destination);
        item.setCurrentLocation(ItemLocation.ground());
        try {
            storage.items().placeOnGround(item);
        } catch (RuntimeException failure) {
            item.setCurrentLocation(origin);
            item.setLocation(position);
            throw failure;
        }
    }

    public static boolean hasSafeAncestry(UOItem item, UOItem target, WorldStorage storage,
                                          Predicate<UOItem> isBlocked) {
        var visited = new HashSet<Integer>();
        var current = target;
        while (visited.add(current.getSerialId())) {
            if (current.getSerialId() == item.getSerialId() || isBlocked.test(current)) {
                return false;
            }
            if (!(current.getCurrentLocation() instanceof ContainerLocation inside)) {
                return true;
            }
            var parent = storage.items().getContainer(inside.containerSerialId());
            if (parent.isEmpty()) {
                return false;
            }
            current = parent.get();
        }
        return false;
    }

    /** Resolves and validates the move before any state changes. */
    public static Runnable prepareContainerMove(UOItem item, UOContainer destination, Location position,
                                                WorldStorage storage, WorldMobile mobiles, Runnable place) {
        if (destination.isOrphan() || !hasSafeAncestry(item, destination, storage, target -> false)) {
            throw new IllegalStateException("Invalid container destination");
        }
        var origin = item.getCurrentLocation();
        var previousPosition = new PointInTheWorld(item);
        var restore = restoreOrigin(item, origin, storage, mobiles);
        return () -> {
            detach(item, storage, mobiles, true);
            try {
                item.setLocation(position);
                place.run();
            } catch (RuntimeException failure) {
                try {
                    destination.removeItemFromContainer(item);
                    item.setLocation(previousPosition);
                    item.setCurrentLocation(origin);
                    restore.run();
                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
        };
    }

    private static Runnable restoreOrigin(UOItem item, ItemLocation origin, WorldStorage storage,
                                          WorldMobile mobiles) {
        return switch (origin) {
            case GroundLocation ground -> () -> storage.items().placeOnGround(item);
            case OrphanLocation orphan -> () -> { };
            case ContainerLocation inside -> {
                var parent = storage.items().getContainer(inside.containerSerialId())
                        .orElseThrow(() -> new IllegalStateException("Container not found"));
                if (!parent.getContainerItems().contains(item.getSerialId())) {
                    throw new IllegalStateException("Item is missing from its origin container");
                }
                yield () -> parent.addItemToContainer(item);
            }
            case EquippedLocation equipped -> {
                var owner = storage.mobiles().get(equipped.ownerSerialId())
                        .orElseThrow(() -> new IllegalStateException("Owner not found"));
                if (!Objects.equals(owner.getEquippedItems().get(item.getLayer()), item.getSerialId())) {
                    throw new IllegalStateException("Item is missing from its equipment slot");
                }
                yield () -> equip(owner, item, mobiles);
            }
        };
    }

    public static void placeInContainer(UOContainer container, UOItem item) {
        container.addItemToContainer(item);
        item.setCurrentLocation(ItemLocation.container(container.getSerialId()));
    }
}
