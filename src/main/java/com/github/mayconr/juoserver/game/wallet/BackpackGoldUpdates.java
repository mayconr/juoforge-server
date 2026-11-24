package com.github.mayconr.juoserver.game.wallet;

import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.game.model.event.ItemInventoryChanged;
import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class BackpackGoldUpdates {
    private final Wallet wallet;
    private final MobileModule mobiles;
    private final WorldStorage storage;

    public BackpackGoldUpdates(Wallet wallet, MobileModule mobiles, WorldStorage storage) {
        this.wallet = wallet;
        this.mobiles = mobiles;
        this.storage = storage;
    }

    public void register(EventBus events) {
        events.register(ItemInventoryChanged.class, event -> refresh(event.item(), event.origin(), event.destination()));
        events.register(ItemUpdated.class, event -> refresh(event.item(), event.item().getCurrentLocation()));
        events.register(ItemDeleted.class, event -> refresh(event.item(), event.item().getCurrentLocation()));
    }

    private void refresh(UOItem item, ItemLocation... affectedLocations) {
        if (!wallet.isGold(item)) {
            return;
        }
        Stream.of(affectedLocations)
                .distinct()
                .flatMap(location -> backpackOwner(location).stream())
                .forEach(mobiles::recalculateGold);
    }

    private Optional<UOMobile> backpackOwner(ItemLocation location) {
        if (!(location instanceof ContainerLocation inside) || inside.containerSerialId() == null) {
            return Optional.empty();
        }
        return storage.items().getContainer(inside.containerSerialId())
                .flatMap(container -> container.getCurrentLocation() instanceof EquippedLocation equipped
                        ? storage.mobiles().get(equipped.ownerSerialId())
                        : Optional.empty())
                .filter(owner -> Objects.equals(owner.getBackpack(), inside.containerSerialId()));
    }
}
