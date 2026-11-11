package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.infrastructure.storage.account.Accounts;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.Mobiles;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Responsibility views over the deprecated flat {@link WorldStorage} methods. Transitional:
 * once callers use the views, each implementation moves here and the flat methods are removed.
 */
@SuppressWarnings("deprecation")
final class WorldStorageViews {

    private WorldStorageViews() {
    }

    record AccountsView(WorldStorage storage) implements Accounts {

        AccountsView {
            Objects.requireNonNull(storage, "World storage is required");
        }

        @Override
        public CompletableFuture<UOAccount> findByUsername(String username) {
            return storage.getAccountByUsername(username);
        }

        @Override
        public CompletableFuture<List<AccountMobile>> mobiles(UOAccount account) {
            return storage.getAccountMobiles(account);
        }
    }

    record SerialsView(WorldStorage storage) implements Serials {

        SerialsView {
            Objects.requireNonNull(storage, "World storage is required");
        }

        @Override
        public CompletableFuture<Integer> nextItem() {
            return storage.getNextItemSerial();
        }

        @Override
        public CompletableFuture<Integer> nextMobile() {
            return storage.getNextMobileSerial();
        }
    }

    record MobilesView(WorldStorage storage) implements Mobiles {

        MobilesView {
            Objects.requireNonNull(storage, "World storage is required");
        }

        @Override
        public UOMobile create(UOMobileData data) {
            return storage.createMobile(data);
        }

        @Override
        public CompletableFuture<UOPlayer> saveNewPlayer(
                int currentMobileSerialId,
                UOPlayer player,
                int currentItemSerialId,
                List<UOItem> starterItems
        ) {
            return storage.saveNewPlayerMobile(currentMobileSerialId, player, currentItemSerialId, starterItems);
        }

        @Override
        public CompletableFuture<UOMobile> load(int serialId) {
            return storage.loadMobile(serialId);
        }

        @Override
        public void unload(UOMobile mobile) {
            storage.unloadMobile(mobile);
        }

        @Override
        public Optional<UOMobile> get(int serialId) {
            return storage.getMobile(serialId);
        }

        @Override
        public UOMobile cache(UOMobile mobile) {
            return storage.cache(mobile);
        }

        @Override
        public List<UOMobile> inRange(Location location, int radius, Predicate<UOMobile> filter) {
            return storage.getMobilesInRange(location, radius, filter);
        }

        @Override
        public List<UOMobile> atLocation(Location location) {
            return storage.getMobilesAtLocation(location);
        }

        @Override
        public List<UOMobile> atLocation(Location location, boolean alive) {
            return storage.getMobilesAtLocation(location, alive);
        }

        @Override
        public void updateLocation(UOMobile mobile, Location oldLocation, Location newLocation) {
            storage.updateMobileLocation(mobile, oldLocation, newLocation);
        }

        @Override
        public void delete(UOMobile mobile) {
            storage.deleteMobile(mobile);
        }

        @Override
        public CompletableFuture<Void> delete(int serialId) {
            return storage.deleteMobile(serialId);
        }

        @Override
        public CompletableFuture<Boolean> existsByName(String name) {
            return storage.mobileExists(name);
        }

        @Override
        public CompletableFuture<Collection<UOMobile>> save() {
            return storage.saveMobiles();
        }
    }

    record ItemsView(WorldStorage storage) implements Items {

        ItemsView {
            Objects.requireNonNull(storage, "World storage is required");
        }

        @Override
        public UOItem create(UOItemData data) {
            return storage.createItem(data);
        }

        @Override
        public CompletableFuture<UOItem> load(int serialId) {
            return storage.loadItem(serialId);
        }

        @Override
        public CompletableFuture<List<UOItem>> loadContainerItems(Container container) {
            return storage.loadContainerItems(container);
        }

        @Override
        public Optional<UOItem> get(int serialId) {
            return storage.getItem(serialId);
        }

        @Override
        public Optional<UOContainer> getContainer(int serialId) {
            return storage.getContainer(serialId);
        }

        @Override
        public UOItem cache(UOItem item) {
            return storage.cache(item);
        }

        @Override
        public List<UOItem> inRange(Location location) {
            return storage.getItemsInRange(location);
        }

        @Override
        public void placeOnGround(UOItem item) {
            storage.placeOnTheGround(item);
        }

        @Override
        public void removeFromGround(UOItem item) {
            storage.removeFromTheGround(item);
        }

        @Override
        public void delete(UOItem item) {
            storage.deleteItem(item);
        }

        @Override
        public CompletableFuture<Collection<UOItem>> save() {
            return storage.saveItems();
        }
    }
}
