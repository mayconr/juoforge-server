package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
public class CachedRealmStorage implements RealmStorage {

    private final MobileStorage mobileStorage;
    private final MobileCache mobileCache = new MobileCache();
    private final WorldMobileIndex worldMobileIndex = new WorldMobileIndex();

    private final ItemStorage itemStorage;
    private final ItemCache itemCache = new ItemCache();
    private final WorldItemIndex worldItemIndex = new WorldItemIndex();

    private final List<UOMobile> dirtyMobiles = new ArrayList<>();
    private final List<UOItem> dirtyItems = new ArrayList<>();

    private Supplier<Integer> itemSerialSupplier;
    private Supplier<Integer> mobileSerialSupplier;

    public CachedRealmStorage(
            MobileStorage mobileStorage,
            ItemStorage itemStorage) {
        this.mobileStorage = mobileStorage;
        this.itemStorage = itemStorage;
    }

    @Override
    public void initialize(Supplier<Integer> itemSerialSupplier, Supplier<Integer> mobileSerialSupplier) {
        this.itemSerialSupplier = itemSerialSupplier;
        this.mobileSerialSupplier = mobileSerialSupplier;

        mobileStorage.loadNPCs()
            .thenCombine(itemStorage.loadGroundItems(), InitialData::new)
            .thenAccept(data->{
                mobileCache.putAll(data.mobiles());
                worldMobileIndex.addAll(data.mobiles());

                itemCache.putAll(data.items());
                worldItemIndex.addAll(data.items());
            })
            .whenComplete(this::logging);
    }

    private record InitialData(List<UOMobile> mobiles, List<UOItem> items) { }

    @Override
    public CompletableFuture<Integer> getNextItemSerial() {
        return itemStorage.getNextItemSerial();
    }

    @Override
    public CompletableFuture<Integer> getNextMobileSerial() {
        return mobileStorage.getNextMobileSerial();
    }

    @Override
    public CompletableFuture<Optional<UOMobile>> findMobileBySerialId(int serialId) {
        var cached = mobileCache.get(serialId);
        if (cached != null) {
            log.info("Mobile [{}] recovered from cache", serialId);
            return CompletableFuture.completedFuture(Optional.of(cached));
        }

        return mobileStorage.findMobileBySerialId(serialId)
                .thenCompose(mobOpt -> mobOpt
                        .map(this::loadAndCacheMobile)
                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));
    }

    private CompletableFuture<Optional<UOMobile>> loadAndCacheMobile(UOMobile mobile) {
        return itemStorage.loadEquippedItems(mobile)
                .thenApply(itemCache::putAll)
                .thenApply(items -> {
                    items.forEach(mobile::equipItem);
                    mobileCache.put(mobile);
                    return Optional.of(mobile);
                }).whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<Optional<UOItem>> findItemBySerialId(int serialId) {
        UOItem cached = itemCache.get(serialId);
        if (cached != null) {
            return CompletableFuture.completedFuture(Optional.of(cached));
        }

        return itemStorage.findItemBySerialId(serialId)
                .thenApply(item -> item.map(this::cacheItem))
                .whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<Optional<UOItem>> findItemByName(String name) {
        return itemStorage.findItemByName(name)
                .thenApply(item->item.map(this::cacheItem))
                .whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<Optional<Container>> findContainerBySerialId(int serialId) {
        return findMobileBySerialId(serialId)
                .thenCompose(mobileOpt -> mobileOpt.map(mobile -> CompletableFuture.completedFuture(Optional.of((Container) mobile)))
                .orElseGet(() -> findItemBySerialId(serialId)
                    .thenApply(itemOpt ->
                            itemOpt.map(item -> (Container) item)
                    )))
                .whenComplete(this::logging);
    }

    @Override
    public List<UOCity> getCities() {
        return List.of(
                new UOCity("Vesper", "Vesper", new PointInTheWorld(2893, 686, 0)),
                new UOCity("Britain", "Britannia", new PointInTheWorld(1478, 1711, 0)));
    }

    @Override
    public CompletableFuture<List<UOMobile>> getMobilesInRange(Location location) {
        List<Integer> serials =
                worldMobileIndex.getSerialsInRange(location);

        List<UOMobile> result = new ArrayList<>(serials.size());

        for (Integer serial : serials) {
            UOMobile mobile = mobileCache.get(serial);
            if (mobile != null) {
                result.add(mobile);
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public void updateMobileLocation(UOMobile mobile, Location oldLoc, Location newLoc) {
        synchronized (this) {
            worldMobileIndex.remove(mobile, oldLoc);
            worldMobileIndex.add(mobile, newLoc);
        }
    }

    @Override
    public void deleteMobile(UOMobile mobile) {
        mobileCache.remove(mobile);
        worldMobileIndex.remove(mobile);

        // add mobile to be deleted from database
        dirtyMobiles.add(mobile);
        log.info("Mobile [{}-{}] added to be removed", mobile.getSerialId(), mobile.getName());
    }

    @Override
    public CompletableFuture<Boolean> mobileExists(String name) {
        return mobileStorage.mobileExists(name);
    }

    @Override
    public CompletableFuture<UOPlayer> createNewPlayer(UOMobile mobile) {
        return mobileStorage.saveMobileFull(mobile)
                .thenApply(mob->(UOPlayer) this.cacheMobile(mobile))
                .thenApply(mob->{
                    for (UOItem item : mob.getEquippedItems().values()) {
                        cacheItem(item);
                    }
                    return mob;
                }).whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<UONpc> createNpc(UONpc mobile) {
        return CompletableFuture.supplyAsync(()->{
            mobileCache.put(mobile);
            worldMobileIndex.add(mobile);
            return mobile;
        });
    }

    @Override
    public CompletableFuture<UOItem> createItem(UOItem item) {
        return CompletableFuture.supplyAsync(()-> cacheItem(item));
    }

    @Override
    public void dropItemOnTheGround(UOItem item) {
        itemCache.put(item);
        worldItemIndex.add(item);
    }

    @Override
    public void removeItemFromTheGround(UOItem item) {
        worldItemIndex.remove(item);
    }

    @Override
    public CompletableFuture<List<UOItem>> getItemsInRange(Location location) {
        final var serials = worldItemIndex.getSerialsInRange(location);

        List<UOItem> result = new ArrayList<>(serials.size());

        for (Integer serial : serials) {
            UOItem item = itemCache.get(serial);
            if (item != null) {
                result.add(item);
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<List<UOItem>> loadContainerItems(Container container) {
        return itemStorage.loadContainerItems(container)
                .thenApply(items->{
                    itemCache.putAll(items);
                    return items;
                })
                .whenComplete(this::logging);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemCache.remove(item);
        worldItemIndex.remove(item);
        dirtyItems.add(item);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobileRuntime() {
        return mobileStorage.saveRuntime(mobileCache.getMobiles())
                .whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobileVitals() {
        return mobileStorage.saveVitals(mobileCache.getMobiles())
                .whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobileAttributes() {
        return mobileStorage.saveAttributes(mobileCache.getMobiles())
                .whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobiles() {
        // TODO remove dirty
        return mobileStorage.saveMobiles(mobileSerialSupplier.get(), mobileCache.getMobiles(), dirtyMobiles)
                .whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<Collection<UOItem>> saveItems() {
        return itemStorage.saveItems(itemSerialSupplier.get(), itemCache.getItems(), dirtyItems)
                .whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<Collection<UOItem>> saveItemStates() {
        return itemStorage.saveStates(itemCache.getItems())
                .thenApply(items->{
                    log.info("Items states saved!");
                    return items;
                }).whenComplete(this::logging);
    }

    private UOItem cacheItem(UOItem item) {
        itemCache.put(item);
        worldItemIndex.add(item);
        return item;
    }

    private UOMobile cacheMobile(UOMobile mobile) {
        mobileCache.put(mobile);
        worldMobileIndex.add(mobile);
        return mobile;
    }

    private <T> void logging(T data, Throwable throwable) {
        if (throwable != null) {
            log.error("Unable to save mobiles", throwable);
        }
    }

}
