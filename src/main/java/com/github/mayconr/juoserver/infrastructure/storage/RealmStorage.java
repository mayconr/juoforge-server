package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface RealmStorage {

    void initialize(Supplier<Integer> itemSerialSupplier, Supplier<Integer> mobileSerialSupplier);

    CompletableFuture<Integer> getNextItemSerial();

    CompletableFuture<Integer> getNextMobileSerial();

    CompletableFuture<UOMobile> findMobileBySerialId(int serialId);

    CompletableFuture<UOItem> findItemBySerialId(int serialId);

    CompletableFuture<UOItem> findItemByName(String name);

    CompletableFuture<Container> findContainerBySerialId(int serialId);

    List<UOCity> getCities();

    CompletableFuture<List<UOMobile>> getMobilesInRange(Location location);

    void updateMobileLocation(UOMobile mobile, Location oldLoc, Location newLoc);

    void deleteMobile(UOMobile mobile);

    CompletableFuture<Boolean> mobileExists(String name);

    CompletableFuture<UOPlayer> createNewPlayer(UOMobile mobile);

    CompletableFuture<UONpc> createNpc(UONpc npc);

    CompletableFuture<UOItem> createItem(UOItem item);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    void dropItemOnTheGround(UOItem item);

    void removeItemFromTheGround(UOItem item);

    CompletableFuture<List<UOItem>> getItemsInRange(Location location);

    void deleteItem(UOItem item);


    CompletableFuture<Collection<UOMobile>> saveMobileRuntime();

    CompletableFuture<Collection<UOMobile>> saveMobileVitals();

    CompletableFuture<Collection<UOMobile>> saveMobileAttributes();

    CompletableFuture<Collection<UOMobile>> saveMobiles();

    CompletableFuture<Collection<UOItem>> saveItems();

    CompletableFuture<Collection<UOItem>> saveItemStates();
}
