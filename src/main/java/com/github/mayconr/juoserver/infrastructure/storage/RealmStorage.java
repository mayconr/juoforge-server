package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
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

    /**
     * Loads an item from the database by its serial id and stores it in the cache.
     *
     * @param serialId the unique serial identifier of the item
     * @return a CompletableFuture containing the loaded {@link UOItem}
     */
    CompletableFuture<UOItem> loadItem(int serialId);

    UOItem loadItem(int serialId, ItemTemplate template);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    void unloadMobile(UOMobile mobile);

    CompletableFuture<List<AccountMobile>> getAccountMobiles(UOAccount uoAccount);

    // Cached lookups
    Optional<UOMobile> getMobile(int serialId);

    /**
     * Retrieves an item from the cache by its serial id.
     * This method does not perform any database access.
     *
     * @param serialId the unique serial identifier of the item
     * @return an Optional containing the cached {@link UOItem} if present, otherwise empty
     */
    Optional<UOItem> getItem(int serialId);

    Optional<UOContainer> getContainer(int serialId);

    // Cache and indexing
    void cache(UOMobile mobile);

    void cache(UOItem item);

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
