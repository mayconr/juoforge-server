package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.infrastructure.storage.account.Accounts;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.Mobiles;
import com.github.mayconr.juoserver.infrastructure.storage.property.Properties;
import com.github.mayconr.juoserver.infrastructure.storage.serial.Serials;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Single entry point for loading and saving the world. It owns the lifecycle and exposes
 * each responsibility through a dedicated view: {@link #accounts()}, {@link #serials()},
 * {@link #mobiles()}, {@link #items()} and {@link #properties()}.
 * <p>
 * The flat methods below the views are deprecated; new code must use the views, and callers
 * should be migrated whenever their code changes.
 */
public interface WorldStorage {

    // =========================
    // LIFECYCLE
    // =========================

    /** Loads the persisted state before the world uses it. */
    void initialize(
            Supplier<Integer> itemSerialSupplier,
            Supplier<Integer> mobileSerialSupplier,
            Consumer<InitialData> updateMobile
    );

    /**
     * Saves mobiles, items and the world properties changed since the last successful save.
     * Completes only after all saves finish; any failure completes the result exceptionally.
     * Saves use independent transactions. Overlapping calls share the active save.
     */
    CompletableFuture<Void> saveWorld();

    // =========================
    // RESPONSIBILITIES
    // =========================

    Accounts accounts();

    Serials serials();

    Mobiles mobiles();

    Items items();

    /**
     * Typed key-value properties of the world. They are loaded with the world storage
     * and persisted by {@link #saveWorld()}; accessing them never touches the database.
     */
    Properties properties();

    // =========================
    // DEPRECATED: ACCOUNTS
    // =========================

    /** @deprecated Use {@link Accounts#findByUsername(String)} through {@link #accounts()}. */
    @Deprecated
    CompletableFuture<UOAccount> getAccountByUsername(String username);

    /** @deprecated Use {@link Accounts#mobiles(UOAccount)} through {@link #accounts()}. */
    @Deprecated
    CompletableFuture<List<AccountMobile>> getAccountMobiles(UOAccount uoAccount);

    // =========================
    // DEPRECATED: SERIALS
    // =========================

    /** @deprecated Use {@link Serials#nextItem()} through {@link #serials()}. */
    @Deprecated
    CompletableFuture<Integer> getNextItemSerial();

    /** @deprecated Use {@link Serials#nextMobile()} through {@link #serials()}. */
    @Deprecated
    CompletableFuture<Integer> getNextMobileSerial();

    // =========================
    // DEPRECATED: MOBILES
    // =========================

    /** @deprecated Use {@link Mobiles#create(UOMobileData)} through {@link #mobiles()}. */
    @Deprecated
    UOMobile createMobile(UOMobileData data);

    /** @deprecated Use {@link Mobiles#saveNewPlayer} through {@link #mobiles()}. */
    @Deprecated
    CompletableFuture<UOPlayer> saveNewPlayerMobile(
            int currentMobileSerialId,
            UOPlayer player,
            int currentItemSerialId,
            List<UOItem> starterItems
    );

    /** @deprecated Use {@link Mobiles#load(int)} through {@link #mobiles()}. */
    @Deprecated
    CompletableFuture<UOMobile> loadMobile(int serialId);

    /** @deprecated Use {@link Mobiles#unload(UOMobile)} through {@link #mobiles()}. */
    @Deprecated
    void unloadMobile(UOMobile mobile);

    /** @deprecated Use {@link Mobiles#get(int)} through {@link #mobiles()}. */
    @Deprecated
    Optional<UOMobile> getMobile(Integer serialId);

    /** @deprecated Use {@link Mobiles#cache(UOMobile)} through {@link #mobiles()}. */
    @Deprecated
    UOMobile cache(UOMobile mobile);

    /** @deprecated Use {@link Mobiles#inRange} through {@link #mobiles()}. */
    @Deprecated
    List<UOMobile> getMobilesInRange(Location location, int radius, Predicate<UOMobile> filter);

    /** @deprecated Use {@link Mobiles#atLocation(Location)} through {@link #mobiles()}. */
    @Deprecated
    List<UOMobile> getMobilesAtLocation(Location location);

    /** @deprecated Use {@link Mobiles#atLocation(Location, boolean)} through {@link #mobiles()}. */
    @Deprecated
    List<UOMobile> getMobilesAtLocation(Location location, boolean alive);

    /** @deprecated Use {@link Mobiles#updateLocation} through {@link #mobiles()}. */
    @Deprecated
    void updateMobileLocation(
            UOMobile mobile,
            Location oldLoc,
            Location newLoc
    );

    /** @deprecated Use {@link Mobiles#delete(UOMobile)} through {@link #mobiles()}. */
    @Deprecated
    void deleteMobile(UOMobile mobile);

    /** @deprecated Use {@link Mobiles#delete(int)} through {@link #mobiles()}. */
    @Deprecated
    CompletableFuture<Void> deleteMobile(int serialId);

    /** @deprecated Use {@link Mobiles#existsByName(String)} through {@link #mobiles()}. */
    @Deprecated
    CompletableFuture<Boolean> mobileExists(String name);

    /** @deprecated Use {@link Mobiles#save()} through {@link #mobiles()}. */
    @Deprecated
    CompletableFuture<Collection<UOMobile>> saveMobiles();

    // =========================
    // DEPRECATED: ITEMS
    // =========================

    /** @deprecated Use {@link Items#create(UOItemData)} through {@link #items()}. */
    @Deprecated
    UOItem createItem(UOItemData data);

    /** @deprecated Use {@link Items#load(int)} through {@link #items()}. */
    @Deprecated
    CompletableFuture<UOItem> loadItem(int serialId);

    /** @deprecated Use {@link Items#loadContainerItems(Container)} through {@link #items()}. */
    @Deprecated
    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    /** @deprecated Use {@link Items#get(int)} through {@link #items()}. */
    @Deprecated
    Optional<UOItem> getItem(int serialId);

    /** @deprecated Use {@link Items#getContainer(int)} through {@link #items()}. */
    @Deprecated
    Optional<UOContainer> getContainer(int serialId);

    /** @deprecated Use {@link Items#cache(UOItem)} through {@link #items()}. */
    @Deprecated
    UOItem cache(UOItem item);

    /** @deprecated Use {@link Items#inRange(Location)} through {@link #items()}. */
    @Deprecated
    List<UOItem> getItemsInRange(Location location);

    /** @deprecated Use {@link Items#placeOnGround(UOItem)} through {@link #items()}. */
    @Deprecated
    void placeOnTheGround(UOItem item);

    /** @deprecated Use {@link Items#removeFromGround(UOItem)} through {@link #items()}. */
    @Deprecated
    void removeFromTheGround(UOItem item);

    /** @deprecated Use {@link Items#delete(UOItem)} through {@link #items()}. */
    @Deprecated
    void deleteItem(UOItem item);

    /** @deprecated Use {@link Items#save()} through {@link #items()}. */
    @Deprecated
    CompletableFuture<Collection<UOItem>> saveItems();
}
