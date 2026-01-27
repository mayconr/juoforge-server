package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ItemStorage {

    CompletableFuture<Integer> getNextItemSerial();

    CompletableFuture<Void> setNextItemSerial(int serial);

    CompletableFuture<List<UOItem>> loadEquippedItems(UOMobile mobile);

    CompletableFuture<List<UOItem>> loadGroundItems();

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    CompletableFuture<UOItem> findItemBySerialId(int serialId);

    CompletableFuture<UOItem> findItemByName(String name);

    CompletableFuture<UOItem> saveItemFull(UOItem item);

    CompletableFuture<Collection<UOItem>> saveItems(int serial, Collection<UOItem> items, Collection<UOItem> dirties);

    CompletableFuture<Collection<UOItem>> saveStates(Collection<UOItem> items);
}
