package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface RealmStorage {

    void initialize(Supplier<Integer> itemSerialSupplier, Supplier<Integer> mobileSerialSupplier, Consumer<InitialData> updateMobile);

    CompletableFuture<UOMobile> loadMobile(int serialId);

    void unloadMobile(UOMobile mobile);

    CompletableFuture<UOItem> loadItem(int serialId);

    Optional<UOMobile> getMobileBySerialId(int serialId);

    Optional<UOItem> getItemBySerialId(int serialId);

    Optional<Container> getContainerBySerialId(int serialId);

    CompletableFuture<Integer> getNextItemSerial();

    CompletableFuture<Integer> getNextMobileSerial();

    @Deprecated
    CompletableFuture<UOMobile> findMobileBySerialId(int serialId);

    @Deprecated
    CompletableFuture<UOItem> findItemBySerialId(int serialId);

    @Deprecated
    CompletableFuture<Container> findContainerBySerialId(int serialId);

    List<UOCity> getCities();

    List<UOMobile> getMobilesInRange(Location location, int radius);

    void updateMobileLocation(UOMobile mobile, Location oldLoc, Location newLoc);

    void deleteMobile(UOMobile mobile);

    CompletableFuture<Boolean> mobileExists(String name);

    CompletableFuture<UOPlayer> insertNewPlayer(int mobileSerialId, int itemSerialId, UOMobile mobile);

    void cacheNpc(UONpc npc);

    void cacheItem(UOItem npc);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    void dropItemOnTheGround(UOItem item);

    void removeItemFromTheGround(UOItem item);

    List<UOItem> getItemsInRange(Location location);

    void deleteItem(UOItem item);

    CompletableFuture<Collection<UOMobile>> saveMobileRuntime();

    CompletableFuture<Collection<UOMobile>> saveMobileVitals();

    CompletableFuture<Collection<UOMobile>> saveMobileAttributes();

    CompletableFuture<Collection<UOMobile>> saveMobiles();

    CompletableFuture<Collection<UOItem>> saveItems();

    CompletableFuture<Collection<UOItem>> saveItemStates();

    boolean isInRange(Location location1, Location location2, int radius);
}
