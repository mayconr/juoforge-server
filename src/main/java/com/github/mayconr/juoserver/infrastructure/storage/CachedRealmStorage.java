package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Slf4j
public class CachedRealmStorage implements RealmStorage {

    private final MobileStorage mobileStorage;
    private final MobileCache mobileCache = new MobileCache();
    private final WorldMobileIndex worldMobileIndex = new WorldMobileIndex();

    private final ItemStorage itemStorage;
    private final ItemCache itemCache = new ItemCache();
    private final WorldItemIndex worldItemIndex = new WorldItemIndex();

    private final AccountStorage accountStorage;

    private final List<UOMobile> dirtyMobiles = new ArrayList<>();
    private final List<UOItem> dirtyItems = new ArrayList<>();

    private Supplier<Integer> itemSerialSupplier;
    private Supplier<Integer> mobileSerialSupplier;

    // Lifecycle
    @Override
    public void initialize(Supplier<Integer> itemSerialSupplier, Supplier<Integer> mobileSerialSupplier, Consumer<InitialData> initialDataConsumer) {
        this.itemSerialSupplier = itemSerialSupplier;
        this.mobileSerialSupplier = mobileSerialSupplier;

        mobileStorage.findAllNpcs()
            .thenCombine(itemStorage.findAllGroundItems().thenApply(ItemMapper::mapToItem), InitialData::new)
            .thenAccept(data->{
                for (UOMobile mobile : data.npcs()) {
                    loadAndCacheMobile(mobile);
                }
                for (UOItem item : data.items()) {
                    itemCache.put(item);
                    worldItemIndex.add(item);
                }

                // World initialized
                initialDataConsumer.accept(data);
            })
            .whenComplete(this::logging);
    }

    // Serial allocation
    @Override
    public CompletableFuture<Integer> getNextItemSerial() {
        return itemStorage.findNextItemSerial();
    }

    @Override
    public CompletableFuture<Integer> getNextMobileSerial() {
        return mobileStorage.findNextMobileSerial();
    }

    // Load and unload
    @Override
    public CompletableFuture<UOMobile> loadMobile(int serialId) {
        return mobileStorage.findMobileBySerialId(serialId)
                .thenCompose(this::loadAndCacheMobile);
    }

    @Override
    public CompletableFuture<UOItem> loadItem(int serialId) {
        return itemStorage.findItemBySerialId(serialId)
                .thenApply(ItemMapper::mapToItem)
                .thenApply(item -> {
                    cache(item);
                    return item;
                })
            .whenComplete(this::logging);
    }

    @Override
    public UOItem loadItem(int serialId, ItemTemplate template) {
        var data = template.createData(serialId);
        var item = ItemMapper.mapToItem(data);

        itemCache.put(item);

        return item;
    }

    @Override
    public CompletableFuture<List<UOItem>> loadContainerItems(Container container) {
        return itemStorage.loadContainerItems(container.getSerialId())
                .thenApply(ItemMapper::mapToItem)
                .thenApply(items -> {
                    itemCache.putAll(items);
                    return items;
                })
                .whenComplete(this::logging);
    }

    @Override
    public void unloadMobile(UOMobile mobile) {
        // TODO save mobile to database
        mobileCache.remove(mobile);
        for (UOItem item : mobile.getContainerItems()) {
            itemCache.remove(item);
        }
        for (UOItem item : mobile.getEquippedItems().values()) {
            itemCache.remove(item);
        }
        log.info("Unloaded mobile {}", mobile);
    }

    @Override
    public CompletableFuture<List<AccountMobile>> getAccountMobiles(UOAccount uoAccount) {
        return accountStorage.findAccountMobiles(Objects.requireNonNull(uoAccount.getId(), "Account id is null"));
    }

    // Cached lookups
    @Override
    public Optional<UOMobile> getMobile(int serialId) {
        return Optional.ofNullable(mobileCache.get(serialId));
    }

    @Override
    public Optional<UOItem> getItem(int serialId) {
        return Optional.ofNullable(itemCache.get(serialId));
    }

    @Override
    public Optional<UOContainer> getContainer(int serialId) {
        if (UOMobile.isMobile(serialId)) {
            return getMobile(serialId).map(UOMobile::getBackpack);
        }
        if (UOItem.isItem(serialId)) {
            return getItem(serialId).map(UOContainer.class::cast);
        }
        return Optional.empty();
    }

    // Cache and indexing
    @Override
    public void cache(UOMobile mobile) {
        if (mobile == null) {
            return;
        }
        mobileCache.put(mobile);
        worldMobileIndex.add(mobile);
    }

    @Override
    public void cache(UOItem item) {
        if (item == null) {
            return;
        }
        itemCache.put(item);
        if (item.isOnTheGround()) {
            worldItemIndex.add(item);
        }
    }

    // Spatial queries
    @Override
    public List<UOMobile> getMobilesInRange(Location location, int radius, Predicate<UOMobile> filter) {
        var serials = worldMobileIndex.getNearbySerials(location, radius);

        if (serials.isEmpty()) {
            return List.of();
        }

        List<UOMobile> result = new ArrayList<>(serials.size());
        for (int serial : serials) {
            UOMobile mobile = mobileCache.get(serial);
            if (mobile == null) {
                continue;
            }

            if (!GameMath.isInRange(mobile, location, radius)) {
                continue;
            }

            if (!filter.test(mobile)) {
                continue;
            }

            result.add(mobile);
        }

        return result;
    }

    @Override
    public List<UOItem> getItemsInRange(Location location) {
        return worldItemIndex.getSerialsInRange(location)
                .stream()
                .map(itemCache::get)
                .toList();
    }

    // State mutation
    @Override
    public void updateMobileLocation(UOMobile mobile, Location oldLoc, Location newLoc) {
        synchronized (this) {
            worldMobileIndex.remove(mobile, oldLoc);
            worldMobileIndex.add(mobile, newLoc);
        }
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
    public void deleteMobile(UOMobile mobile) {
        mobileCache.remove(mobile);
        worldMobileIndex.remove(mobile);

        dirtyMobiles.add(mobile);
        log.info("Mobile [{}-{}] added to be removed", mobile.getSerialId(), mobile.getName());
    }

    @Override
    public CompletableFuture<Void> deleteMobile(int serialId) {
        return mobileStorage.deleteBySerialId(serialId);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemCache.remove(item);
        worldItemIndex.remove(item);
        dirtyItems.add(item);
    }

    // Existence and creation
    @Override
    public CompletableFuture<Boolean> mobileExists(String name) {
        return mobileStorage.mobileExists(name);
    }

    @Override
    public CompletableFuture<UOPlayer> insertPlayerMobile(int mobileSerialId, int itemSerialId, UOPlayer player) {
        return mobileStorage.saveMobileFull(mobileSerialId, itemSerialId, player)
                .thenApply(mobile -> (UOPlayer) mobile)
                .whenComplete(this::logging);
    }

    // Persistence
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
        var updated = itemCache.getItems()
                .stream()
                .map(ItemMapper::mapToData)
                .toList();
        var dirty = dirtyItems.stream()
                .map(ItemMapper::mapToData)
                .toList();

        return itemStorage.saveItems(itemSerialSupplier.get(), updated, dirty)
                .thenApply(ItemMapper::mapToItem);
    }

    @Override
    public CompletableFuture<Collection<UOItem>> saveItemStates() {
        var updated = itemCache.getItems()
                .stream()
                .map(ItemMapper::mapToData)
                .toList();

        return itemStorage.saveStates(updated)
                .thenApply(items->{
                    log.info("Items states saved!");
                    return ItemMapper.mapToItem(items);
                });
    }

    private <T> void logging(T data, Throwable throwable) {
        if (throwable != null) {
            log.error("Unable execute storage", throwable);
        }
    }

    private CompletableFuture<UOMobile> loadAndCacheMobile(UOMobile mobile) {
        return itemStorage.findAllEquippedItems(mobile.getSerialId())
                .thenApply(ItemMapper::mapToItem)
                .thenApply(itemCache::putAll)
                .thenApply(items -> {
                    items.forEach(mobile::equipItem);
                    mobileCache.put(mobile);
                    worldMobileIndex.add(mobile);
                    return mobile;
                })
                .whenComplete(this::logging);
    }

    @Override
    public CompletableFuture<UOAccount> getAccountByUsername(String username) {
        return accountStorage.getByUsername(username);
    }
}
