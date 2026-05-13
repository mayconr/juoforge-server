package com.github.mayconr.juoserver.infrastructure.storage;

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

    private final List<Integer> dirtyMobiles = new ArrayList<>();
    private final List<UOItem> dirtyItems = new ArrayList<>();

    private Supplier<Integer> itemSerialSupplier;
    private Supplier<Integer> mobileSerialSupplier;

    private MobileDependencyLoader mobileDependencyLoader;

    // Lifecycle
    @Override
    public void initialize(Supplier<Integer> itemSerialSupplier, Supplier<Integer> mobileSerialSupplier, Consumer<InitialData> initialDataConsumer) {
        this.itemSerialSupplier = itemSerialSupplier;
        this.mobileSerialSupplier = mobileSerialSupplier;
        this.mobileDependencyLoader = new MobileDependencyLoader(mobileStorage, itemStorage, itemCache);

        // Load all World NPCs
        var loadGroundItems = itemStorage.findAllGroundItems()
                .thenApply(ItemMapper::mapToItem);

        // World initialized
        mobileStorage.findAllNpcs()
            .thenApply(MobileMapper::mapToMobile)
            .thenCombine(loadGroundItems, InitialData::new)
            .thenCompose(data -> {
                var dependencyFutures = data.mobiles().stream()
                        .map(mobile ->
                                mobileDependencyLoader.loadDependencies(mobile)
                                    .thenApply(this::cache)
                                    .exceptionally(ex -> {
                                        log.error("Failed loading {}", mobile, ex);
                                        return null;
                                    })
                        )
                        .toArray(CompletableFuture[]::new);

                return CompletableFuture
                        .allOf(dependencyFutures)
                        .thenApply(v -> data);
            })
            .thenCompose(data->{
                for (UOItem item : data.items()) {
                    itemCache.put(item);
                    worldItemIndex.add(item);
                }
                return CompletableFuture.completedFuture(data);
            })
            .thenAccept(initialDataConsumer)
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
                .thenApply(MobileMapper::mapToMobile)
                .thenCompose(mobileDependencyLoader::loadDependencies)
                .thenApply(this::cache);
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
    public UOItem createItem(UOItemData data) {
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
        var backpack = (UOContainer) itemCache.get(mobile.getBackpack());
        for (Integer itemSerial : backpack.getContainerItems()) {
            itemCache.remove(itemCache.get(itemSerial));
        }
        for (Integer itemSerial : mobile.getEquippedItems().values()) {
            itemCache.remove(itemCache.get(itemSerial));
        }
        log.info("Unloaded mobile {}", mobile);
    }

    @Override
    public CompletableFuture<List<AccountMobile>> getAccountMobiles(UOAccount uoAccount) {
        return accountStorage.findAccountMobiles(Objects.requireNonNull(uoAccount.getId(), "Account id is null"));
    }

    // Cached lookups
    @Override
    public Optional<UOMobile> getMobile(Integer serialId) {
        if (serialId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mobileCache.get(serialId));
    }

    @Override
    public Optional<UOItem> getItem(int serialId) {
        return Optional.ofNullable(itemCache.get(serialId));
    }

    @Override
    public Optional<UOContainer> getContainer(int serialId) {
        if (UOMobile.isMobile(serialId)) {
            return getMobile(serialId).map(UOMobile::getBackpack)
                    .map(itemCache::get)
                    .map(UOContainer.class::cast);
        }
        if (UOItem.isItem(serialId)) {
            return getItem(serialId).map(UOContainer.class::cast);
        }
        return Optional.empty();
    }

    // Cache and indexing
    @Override
    public UOMobile cache(UOMobile mobile) {
        if (mobile == null) {
            return null;
        }
        mobileCache.put(mobile);
        worldMobileIndex.add(mobile);
        return mobile;
    }

    @Override
    public UOItem cache(UOItem item) {
        if (item == null) {
            return null;
        }
        itemCache.put(item);
        if (item.getCurrentLocation() instanceof GroundLocation) {
            worldItemIndex.add(item);
        }

        return item;
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
    public void placeOnTheGround(UOItem item) {
        itemCache.put(item);
        worldItemIndex.add(item);
    }

    @Override
    public void removeFromTheGround(UOItem item) {
        worldItemIndex.remove(item);
    }

    @Override
    public void deleteMobile(UOMobile mobile) {
        mobileCache.remove(mobile);
        worldMobileIndex.remove(mobile);

        dirtyMobiles.add(mobile.getSerialId());
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
    public CompletableFuture<UOPlayer> saveNewPlayerMobile(int currentMobileSerialId, UOPlayer player, int currentItemSerialId, List<UOItem> starterItems) {
        var data = player.toData();
        var itemsData = starterItems.stream().map(UOItem::toData).toList();

        return mobileStorage.saveMobileFull(currentMobileSerialId, data, currentItemSerialId, itemsData)
                .thenApply(MobileMapper::mapToMobile)
                .thenApply(UOPlayer.class::cast).whenComplete((uo, throwable) -> {
                    if (throwable != null) {
                        log.error("Error in inserting mobile player [{}]", player.getName(), throwable);
                    }
                });
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobiles() {
        var data = mobileCache.getMobiles()
                .stream()
                .map(UOMobile::toData)
                .toList();
        return mobileStorage.saveMobiles(mobileSerialSupplier.get(), data, dirtyMobiles)
                .thenApply(MobileMapper::mapToMobile);
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
    public CompletableFuture<UOAccount> getAccountByUsername(String username) {
        return accountStorage.getByUsername(username);
    }

    private <T> void logging(T data, Throwable throwable) {
        if (throwable != null) {
            log.error("Unable execute storage", throwable);
        }
    }

    @Override
    public UOMobile createMobile(UOMobileData data) {
        var mobile = switch (data.getType()) {
            case "N" -> new UONpc(data);
            case "P" -> new UOPlayer(data);
            default -> throw new IllegalArgumentException("Invalid data type");
        };
        return cache(mobile);
    }
}
