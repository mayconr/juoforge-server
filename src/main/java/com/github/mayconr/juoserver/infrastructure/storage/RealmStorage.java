package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface RealmStorage {

    CompletableFuture<UOAccount> getAccountByUsername(String username);

    // Lifecycle
    void initialize(Supplier<Integer> itemSerialSupplier, Supplier<Integer> mobileSerialSupplier, Consumer<InitialData> updateMobile);

    // Serial allocation
    CompletableFuture<Integer> getNextItemSerial();

    CompletableFuture<Integer> getNextMobileSerial();

    // Load and unload
    CompletableFuture<UOMobile> loadMobile(int serialId);

    CompletableFuture<UOItem> loadItem(int serialId);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    void unloadMobile(UOMobile mobile);

    CompletableFuture<List<AccountMobile>> getPlayerMobiles(UOAccount uoAccount);

    // Cached lookups
    Optional<UOMobile> getMobileBySerialId(int serialId);

    Optional<UOItem> getItemBySerialId(int serialId);

    Optional<Container> getContainerBySerialId(int serialId);

    // Cache and indexing
    void cacheMobile(UOMobile mobile);

    void cacheItem(UOItem npc);

    // Spatial queries
    List<UOMobile> getMobilesInRange(Location location, int radius, Predicate<UOMobile> filter);

    List<UOItem> getItemsInRange(Location location);

    // State mutation
    void updateMobileLocation(UOMobile mobile, Location oldLoc, Location newLoc);

    void dropItemOnTheGround(UOItem item);

    void removeItemFromTheGround(UOItem item);

    void deleteMobile(UOMobile mobile);

    CompletableFuture<Void> deleteMobile(int serialId);

    void deleteItem(UOItem item);

    // Existence and creation
    CompletableFuture<Boolean> mobileExists(String name);

    CompletableFuture<UOPlayer> insertPlayerMobile(int mobileSerialId, int itemSerialId, UOPlayer player);

    // Persistence
    CompletableFuture<Collection<UOMobile>> saveMobileRuntime();

    CompletableFuture<Collection<UOMobile>> saveMobileVitals();

    CompletableFuture<Collection<UOMobile>> saveMobileAttributes();

    CompletableFuture<Collection<UOMobile>> saveMobiles();

    CompletableFuture<Collection<UOItem>> saveItems();

    CompletableFuture<Collection<UOItem>> saveItemStates();

}
