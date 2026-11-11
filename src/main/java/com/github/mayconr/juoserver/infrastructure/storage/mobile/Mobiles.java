package com.github.mayconr.juoserver.infrastructure.storage.mobile;

import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/** Mobiles of the world: creation, loading, the in-memory cache, spatial index and persistence. */
public interface Mobiles {

    // =========================
    // CREATION AND LOADING
    // =========================

    UOMobile create(UOMobileData data);

    /** Persists a new player with its starter items, allocating serials from the given values. */
    CompletableFuture<UOPlayer> saveNewPlayer(
            int currentMobileSerialId,
            UOPlayer player,
            int currentItemSerialId,
            List<UOItem> starterItems
    );

    CompletableFuture<UOMobile> load(int serialId);

    void unload(UOMobile mobile);

    // =========================
    // CACHE
    // =========================

    Optional<UOMobile> get(int serialId);

    UOMobile cache(UOMobile mobile);

    // =========================
    // SPATIAL QUERIES
    // =========================

    List<UOMobile> inRange(Location location, int radius, Predicate<UOMobile> filter);

    /** Returns all mobiles at the location, alive or dead; empty when none are found. */
    List<UOMobile> atLocation(Location location);

    /** Returns living mobiles at the location when {@code alive} is true, otherwise dead ones. */
    List<UOMobile> atLocation(Location location, boolean alive);

    void updateLocation(UOMobile mobile, Location oldLocation, Location newLocation);

    // =========================
    // REMOVAL AND PERSISTENCE
    // =========================

    void delete(UOMobile mobile);

    CompletableFuture<Void> delete(int serialId);

    CompletableFuture<Boolean> existsByName(String name);

    /** Saves cached mobiles only; {@link WorldStorage#saveWorld()} saves the whole world. */
    CompletableFuture<Collection<UOMobile>> save();
}
