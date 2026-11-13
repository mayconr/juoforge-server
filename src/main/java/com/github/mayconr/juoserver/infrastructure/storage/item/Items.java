package com.github.mayconr.juoserver.infrastructure.storage.item;

import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Items and containers of the world: creation, loading, the in-memory cache, ground index and persistence. */
public interface Items {

    // =========================
    // CREATION AND LOADING
    // =========================

    UOItem create(UOItemData data);

    CompletableFuture<UOItem> load(int serialId);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    // =========================
    // CACHE
    // =========================

    Optional<UOItem> get(int serialId);

    Optional<UOContainer> getContainer(int serialId);

    UOItem cache(UOItem item);

    // =========================
    // GROUND
    // =========================

    List<UOItem> inRange(Location location);

    /** Snapshot of ground items across all indexed regions. */
    List<UOItem> onGround();

    void placeOnGround(UOItem item);

    void removeFromGround(UOItem item);

    // =========================
    // REMOVAL AND PERSISTENCE
    // =========================

    void delete(UOItem item);

    /** Saves cached items only; {@link WorldStorage#saveWorld()} saves the whole world. */
    CompletableFuture<Collection<UOItem>> save();
}
