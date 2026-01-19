package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.common.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.common.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemFactory;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileFactory;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileStorage;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class CachedWorldService implements WorldService {

    private static final int MOBILES_MAX_SERIAL_ID = 0x3FFFFFFF;
    public static final int OBJECTS_MIN_SERIAL_ID = MOBILES_MAX_SERIAL_ID + 1;
    private static final int OBJECTS_MAX_SERIAL_ID = 0x7FFFFFFF;

    private final NpcTemplateRegistry npcTemplateRegistry;
    private final MobileStorage mobileStorage;
    private final MobileCache mobileCache = new MobileCache();
    private final WorldMobileIndex worldMobileIndex = new WorldMobileIndex();

    private final ItemTemplateRegistry itemTemplateRegistry;
    private final ItemStorage itemStorage;
    private final ItemCache itemCache = new ItemCache();
    private final WorldItemIndex worldItemIndex = new WorldItemIndex();

    public CachedWorldService(
            NpcTemplateRegistry npcTemplateRegistry,
            ItemTemplateRegistry itemTemplateRegistry,
            MobileStorage mobileStorage,
            ItemStorage itemStorage) {
        this.npcTemplateRegistry = npcTemplateRegistry;
        this.itemTemplateRegistry = itemTemplateRegistry;
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
                }).whenComplete(((uoMobile, throwable) -> {
                    if (throwable != null) {
                        log.error("Unable to load equipped items", throwable);
                    }
                }));
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
    public MovementResult tryMove(UOMobile mobile, MoveRequest request) {
        final var direction = request.getDirection();
        Location to;
        if (mobile.getDirection().equals(direction)) {
            to = new PointInTheWorld(mobile.getX() + direction.getDx(), mobile.getY() + direction.getDy(), mobile.getZ());
        } else {
            to = mobile;
        }
        return MovementResult.success(mobile, direction, to, request.isRunning());
    }

    @Override
    public void applyMove(UOMobile mobile, MovementResult result) {
        synchronized (this) {
            worldMobileIndex.remove(mobile);
            mobile.setDirection(result.direction());
            mobile.setRunning(result.running());
            mobile.setLocation(result.to());
            worldMobileIndex.add(mobile);
        }
    }

    @Override
    public void deleteMobile(UOMobile mobile) {
        mobileCache.remove(mobile);
        worldMobileIndex.remove(mobile);
        // TODO remove from database
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
    public CompletableFuture<UONpc> createNpcAtLocation(String name, Location location) {
        final var template = npcTemplateRegistry.get(name);
        if (template == null) {
            return CompletableFuture.completedFuture(null);
        }
        return mobileStorage.saveMobileFull(MobileFactory.createFromTemplate(template, location))
                .thenApply(mobile->{
                    mobileCache.put(mobile);
                    worldMobileIndex.add(mobile);

                    log.info("Npc [{}-{}] created!", mobile.getSerialId(), mobile.getName());
                    return (UONpc) mobile;
                })
                .whenComplete((mobile, throwable)->{
                    if (throwable != null) {
                        log.error("Unable to create new player", throwable);
                    }
                });
    }

    @Override
    public CompletableFuture<UOItem> createItemAtLocation(String name, Location location) {
        final var template = itemTemplateRegistry.get(name);
        if (template == null) {
            return CompletableFuture.completedFuture(null);
        }

        final var createdItem = ItemFactory.createFromTemplate(template, location);
        return itemStorage.saveItemFull(createdItem)
            .thenApply(item->{
                itemCache.put(item);
                worldItemIndex.add(item);
                return item;
            })
            .whenComplete(((item, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to save item {}", name, throwable);
                }
            }));
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
    public CompletableFuture<List<UOItem>> loadGroundItems() {
        return itemStorage.loadGroundItems()
                .thenApply(uoItems -> {
                    itemCache.putAll(uoItems);
                    worldItemIndex.addAll(uoItems);
                    log.info("Ground items added to world cache!");
                    return uoItems;
                });
    }

    @Override
    public CompletableFuture<List<UOItem>> loadContainerItems(Container container) {
        return itemStorage.loadContainerItems(container)
                .thenApply(items->{
                    itemCache.putAll(items);
                    return items;
                });
    }

    @Override
    public boolean isMobile(int serialId) {
        return serialId <= MOBILES_MAX_SERIAL_ID;
    }

    @Override
    public void deleteItem(UOItem item) {
        itemCache.remove(item);
        worldItemIndex.remove(item);
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

    @Override
    public CompletableFuture<Collection<UOItem>> saveItemStates() {
        return itemStorage.saveStates(itemCache.getItems())
                .thenApply(items->{
                    log.info("Items states saved!");
                    return items;
                });
    }
}
