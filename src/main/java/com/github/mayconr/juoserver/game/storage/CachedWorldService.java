package com.github.mayconr.juoserver.game.storage;

import com.github.mayconr.juoserver.game.core.model.*;
import com.github.mayconr.juoserver.game.core.prototype.ItemPrototype;
import com.github.mayconr.juoserver.game.core.prototype.PrototypeManager;
import com.github.mayconr.juoserver.game.storage.item.ItemStorage;
import com.github.mayconr.juoserver.game.storage.mobile.MobileFactory;
import com.github.mayconr.juoserver.game.storage.mobile.MobileStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CachedWorldService implements WorldService {

    private static final int MOBILES_MAX_SERIAL_ID = 0x3FFFFFFF;

    public static final int OBJECTS_MIN_SERIAL_ID = MOBILES_MAX_SERIAL_ID + 1;
    private static final int OBJECTS_MAX_SERIAL_ID = 0x7FFFFFFF;
    private static final List<UOMobile> MOBILES = new ArrayList<>();
    private static final List<UOItem> OBJECTS = new CopyOnWriteArrayList<>();
    private static final AtomicInteger MOBILE_COUNTER = new AtomicInteger(1);
    private static final AtomicInteger OBJECT_COUNTER = new AtomicInteger(OBJECTS_MIN_SERIAL_ID);
    private static final Map<Long, List<UOItem>> GROUNDED_ITEMS = new ConcurrentHashMap<>();

    private final PrototypeManager prototypeManager;
    private final MobileStorage mobileStorage;
    private final ItemStorage itemStorage;

    private final MobileCache mobileCache = new MobileCache();
    private final WorldMobileIndex worldMobileIndex = new WorldMobileIndex();

    private final ItemCache itemCache = new ItemCache();
    private final WorldItemIndex worldItemIndex = new WorldItemIndex();

    public CachedWorldService(
            PrototypeManager prototypeManager,
            MobileStorage mobileStorage,
            ItemStorage itemStorage) {
        this.prototypeManager = prototypeManager;
        this.mobileStorage = mobileStorage;
        this.itemStorage = itemStorage;
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
                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
                );
    }

    private CompletableFuture<Optional<UOMobile>> loadAndCacheMobile(UOMobile mobile) {
        return itemStorage.loadEquippedItems(mobile)
                .thenApply(itemCache::putAll)
                .thenApply(items -> {
                    items.forEach(mobile::equipItem);
                    mobileCache.put(mobile);
                    return Optional.of(mobile);
                });
    }

    @Override
    public CompletableFuture<Optional<UOItem>> findItemBySerialId(int serialId) {
        UOItem cached = itemCache.get(serialId);
        if (cached != null) {
            return CompletableFuture.completedFuture(Optional.of(cached));
        }

        return itemStorage.findItemBySerialId(serialId)
                .thenApply(opt -> opt.map(itemCache::put))
                .whenComplete((opt, throwable)->{
                    opt.ifPresent(item->{
                        log.info("Item [{}={}] loaded from database", item.getSerialId(), item.getName());
                    });
                });
    }

    @Override
    public CompletableFuture<Optional<Container>> findContainerBySerialId(int serialId) {
        return findMobileBySerialId(serialId)
                .thenCompose(mobileOpt -> mobileOpt.map(mobile -> CompletableFuture.completedFuture(Optional.of((Container) mobile)))
                .orElseGet(() -> findItemBySerialId(serialId)
                    .thenApply(itemOpt ->
                            itemOpt.map(item -> (Container) item)
                    )))
                .exceptionally(throwable -> {
                    log.error("Unable do process container by serialId {}", serialId, throwable);
                    return Optional.empty();
                });
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
    public void moveMobile(UOMobile mobile) {
        worldMobileIndex.remove(mobile);
        worldMobileIndex.add(mobile);
    }

    @Override
    public void deleteMobile(UOMobile mobile) {
        MOBILES.remove(mobile);
    }

    @Override
    public CompletableFuture<UOPlayer> createPlayer(PlayerDetails details) {
        return mobileStorage.saveMobileFull(MobileFactory.createNewPlayer(details))
                .thenApply(mobile->{
                    mobileCache.put(mobile);
                    worldMobileIndex.add(mobile);

                    log.info("Player [{}-{}] created!", mobile.getSerialId(), mobile.getName());
                    return (UOPlayer) mobile;
                })
                .whenComplete((mobile, throwable)->{
                    if (throwable != null) {
                        log.error("Unable to create new player", throwable);
                    }
                });
    }

    @Override
    public UONpc createNpcAtLocation(String name, Location location) {
        final var npcPrototype =
                prototypeManager
                        .getNpcByName(name)
                        .orElseThrow(() -> new NpcPrototypeNotFoundException(name));
        final var npc = new UONpc(MOBILE_COUNTER.getAndIncrement(), npcPrototype, location);
        for (Map.Entry<Layer, String> entry : npcPrototype.getEquippedItems().entrySet()) {
            npc.equipItem(entry.getKey(), createItem(entry.getValue()));
        }
        MOBILES.add(npc);
        return npc;
    }

    @Override
    public UOItem createItemOnTheGround(String name, Location location) {
        final var prototype =
                prototypeManager
                        .getItemByName(name)
                        .orElseThrow(() -> new ItemPrototypeNotFoundException(name));
        final var item = createItemByPrototype(prototype, location);

        // geo index
        final var blockX = item.getX() / 24;
        final var blockY = item.getY() / 24;
        final var key = regionKey(blockX, blockY);
        GROUNDED_ITEMS
                .computeIfAbsent(key, aLong -> Collections.synchronizedList(new ArrayList<>(10)))
                .add(item);
        return item;
    }

    @Override
    public UOItem createItem(String name) {
        final var prototype =
                prototypeManager
                        .getItemByName(name)
                        .orElseThrow(() -> new ItemPrototypeNotFoundException(name));
        return createItemByPrototype(prototype, new PointInTheWorld(0, 0, 0));
    }

    @Override
    public UOItem createItem(String name, Location location) {
        final var prototype =
                prototypeManager
                        .getItemByName(name)
                        .orElseThrow(() -> new ItemPrototypeNotFoundException(name));
        return createItemByPrototype(prototype, location);
    }

    private UOItem createItemByPrototype(ItemPrototype prototype, Location location) {
        UOItem item;
        if (ItemType.CONTAINER.equals(prototype.getType())) {
            item = new UOContainer(OBJECT_COUNTER.getAndIncrement(), prototype, location);
        } else {
            item = new UOItem(OBJECT_COUNTER.getAndIncrement(), prototype, location);
        }
        OBJECTS.add(item);
        return item;
    }

    @Override
    public void dropItemOnTheGround(UOItem item) {
        itemCache.put(item);
        worldItemIndex.add(item);
    }

    @Override
    public void removeItemFromTheGround(UOItem item) {
        int blockX = item.getX() / 24;
        int blockY = item.getY() / 24;
        long key = regionKey(blockX, blockY);
        List<UOItem> items =
                GROUNDED_ITEMS.computeIfAbsent(
                        key, aLong -> Collections.synchronizedList(new ArrayList<>(10)));
        synchronized (items) {
            items.remove(item);
        }
    }

    @Override
    public CompletableFuture<List<UOItem>> getItemsInRange(Location location) {
        List<Integer> serials =
                worldItemIndex.getSerialsInRange(location);

        List<UOItem> result = new ArrayList<>(serials.size());

        for (Integer serial : serials) {
            UOItem item = itemCache.get(serial);
            if (item != null) {
                result.add(item);
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    private long regionKey(int x, int y) {
        return (((long) x) << 32) | (y & 0xFFFFFFFFL);
    }

    @Override
    public boolean isMobile(int serialId) {
        return serialId <= MOBILES_MAX_SERIAL_ID;
    }

    @Override
    public void deleteItem(UOItem item) {
        OBJECTS.remove(item);
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobileRuntime() {
        return mobileStorage.saveRuntime(mobileCache.getMobiles())
                .whenComplete((mobiles, throwable)->{
                    if (throwable != null) {
                        log.error("Unable to save mobiles runtime", throwable);
                        return;
                    }
                    log.info("Mobiles runtime saved!");
                });
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobileVitals() {
        return mobileStorage.saveVitals(mobileCache.getMobiles())
                .whenComplete((mobiles, throwable)->{
                    if (throwable != null) {
                        log.error("Unable to save mobiles vitals", throwable);
                        return;
                    }
                    log.info("Mobiles vitals saved!");
                });
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobileAttributes() {
        return mobileStorage.saveAttributes(mobileCache.getMobiles())
                .whenComplete((mobiles, throwable)->{
                    if (throwable != null) {
                        log.error("Unable to save mobiles attributes", throwable);
                        return;
                    }
                    log.info("Mobiles attributes saved!");
                });
    }

    @Override
    public CompletableFuture<Collection<UOMobile>> saveMobiles() {
        return mobileStorage.saveMobiles(mobileCache.getMobiles())
                .whenComplete((mobiles, throwable)->{
                    if (throwable != null) {
                        log.error("Unable to save mobiles", throwable);
                        return;
                    }
                    log.info("Mobiles saved!");
                });
    }
}
