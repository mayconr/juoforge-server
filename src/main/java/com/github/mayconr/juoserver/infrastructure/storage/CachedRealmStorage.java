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

    @Override
    public CompletableFuture<UOMobile> loadMobile(int serialId) {
        return mobileStorage.findMobileBySerialId(serialId)
                .thenCompose(this::loadAndCacheMobile);
    }

    private CompletableFuture<UOMobile> loadAndCacheMobile(UOMobile mobile) {
        return itemStorage.loadEquippedItems(mobile)
                .thenApply(itemCache::putAll)
                .thenApply(items -> {
                    items.forEach(mobile::equipItem);
                    mobileCache.put(mobile);
                    return mobile;
                }).whenComplete(this::logging);
    }

    @Override
    public Optional<UOMobile> getMobileBySerialId(int serialId) {
        return Optional.ofNullable(mobileCache.get(serialId));
    }

    @Override
    public CompletableFuture<UOItem> loadItem(int serialId) {
        return itemStorage.findItemBySerialId(serialId)
            .thenApply(item->{
                cacheItem(item);
                return item;
            })
            .whenComplete(this::logging);
    }

    @Override
    public Optional<UOItem> getItemBySerialId(int serialId) {
        return Optional.ofNullable(itemCache.get(serialId));
    }

    @Override
    public Optional<Container> getContainerBySerialId(int serialId) {
        if (UOMobile.isMobile(serialId)) {
            return getMobileBySerialId(serialId).map(Container.class::cast);
        }
        if (UOItem.isItem(serialId)) {
            return getItemBySerialId(serialId).map(Container.class::cast);
        }
        return Optional.empty();
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
    public CompletableFuture<UOMobile> findMobileBySerialId(int serialId) {
        var cached = mobileCache.get(serialId);
        if (cached != null) {
            if (log.isDebugEnabled()) {
                log.debug("Mobile [{}-{}] recovered from cache", serialId, cached.getName());
            }

            return CompletableFuture.completedFuture(cached);
        }

        return mobileStorage.findMobileBySerialId(serialId)
                .thenCompose(this::loadAndCacheMobile);
    }

    @Override
    public CompletableFuture<UOItem> findItemBySerialId(int serialId) {
        UOItem cached = itemCache.get(serialId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return itemStorage.findItemBySerialId(serialId)
                .thenApply(item->{
                    cacheItem(item);
                    return item;
                })
                .whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<Container> findContainerBySerialId(int serialId) {
        if (UOMobile.isMobile(serialId)) {
            return findMobileBySerialId(serialId)
                    .thenApply(mobile-> (Container) mobile);
        }
        if (UOItem.isItem(serialId)) {
            return findItemBySerialId(serialId).thenApply(item -> (Container) item);
        }
        return CompletableFuture.failedFuture(new IllegalArgumentException("Serial ["+serialId+"] is not valid"));
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

        // add player to be deleted from database
        dirtyMobiles.add(mobile);
        log.info("Mobile [{}-{}] added to be removed", mobile.getSerialId(), mobile.getName());
    }

    @Override
    public CompletableFuture<Boolean> mobileExists(String name) {
        return mobileStorage.mobileExists(name);
    }

    @Override
    public CompletableFuture<UOPlayer> insertNewPlayer(int mobileSerialId, int itemSerialId, UOMobile mobile) {
        return mobileStorage.saveMobileFull(mobileSerialId, itemSerialId, mobile)
                .thenApply(mob->(UOPlayer) this.cacheMobile(mobile))
                .thenApply(mob->{
                    for (UOItem item : mob.getEquippedItems().values()) {
                        cacheItem(item);
                    }
                    return mob;
                }).whenComplete(this::logging);
    }

    @Override
    public void cacheNpc(UONpc npc) {
        mobileCache.put(npc);
        worldMobileIndex.add(npc);
    }

    @Override
    public void cacheItem(UOItem item) {
        itemCache.put(item);
        worldItemIndex.add(item);
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
