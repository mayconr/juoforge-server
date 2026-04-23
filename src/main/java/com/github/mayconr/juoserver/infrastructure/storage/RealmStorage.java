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

    // =========================
    // LIFECYCLE
    // =========================

    void initialize(
            Supplier<Integer> itemSerialSupplier,
            Supplier<Integer> mobileSerialSupplier,
            Consumer<InitialData> updateMobile
    );

    // =========================
    // ACCOUNT ACCESS
    // =========================

    CompletableFuture<UOAccount> getAccountByUsername(String username);

    CompletableFuture<List<AccountMobile>> getAccountMobiles(UOAccount uoAccount);

    // =========================
    // SERIAL ALLOCATION
    // =========================

    CompletableFuture<Integer> getNextItemSerial();

    CompletableFuture<Integer> getNextMobileSerial();

    // =========================
    // CREATION
    // =========================

    UOItem createItem(UOItemData data);

    UOMobile createMobile(UOMobileData data);

    CompletableFuture<UOPlayer> insertPlayerMobile(
            int mobileSerialId,
            int itemSerialId,
            UOPlayer player
    );

    // =========================
    // LOADING
    // =========================

    CompletableFuture<UOMobile> loadMobile(int serialId);

    CompletableFuture<UOItem> loadItem(int serialId);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    void unloadMobile(UOMobile mobile);

    // =========================
    // CACHE LOOKUPS
    // =========================

    Optional<UOMobile> getMobile(Integer serialId);

    Optional<UOItem> getItem(int serialId);

    Optional<UOContainer> getContainer(int serialId);

    // =========================
    // CACHE MANAGEMENT
    // =========================

    UOMobile cache(UOMobile mobile);

    UOItem cache(UOItem item);

    // =========================
    // SPATIAL QUERIES
    // =========================

    List<UOMobile> getMobilesInRange(
            Location location,
            int radius,
            Predicate<UOMobile> filter
    );

    List<UOItem> getItemsInRange(Location location);

    // =========================
    // STATE MUTATION
    // =========================

    void updateMobileLocation(
            UOMobile mobile,
            Location oldLoc,
            Location newLoc
    );

    void placeOnTheGround(UOItem item);

    void removeFromTheGround(UOItem item);

    void deleteMobile(UOMobile mobile);

    CompletableFuture<Void> deleteMobile(int serialId);

    void deleteItem(UOItem item);

    // =========================
    // EXISTENCE CHECKS
    // =========================

    CompletableFuture<Boolean> mobileExists(String name);

    // =========================
    // PERSISTENCE
    // =========================


    CompletableFuture<Collection<UOMobile>> saveMobiles();

    CompletableFuture<Collection<UOItem>> saveItems();

    CompletableFuture<Collection<UOItem>> saveItemStates();

}