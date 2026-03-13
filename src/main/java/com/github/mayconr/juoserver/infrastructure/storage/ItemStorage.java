package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ItemStorage {

    CompletableFuture<Integer> findNextItemSerial();

    CompletableFuture<Void> setNextItemSerial(int serial);

    CompletableFuture<List<UOItem>> findAllEquippedItems(UOMobile mobile);

    CompletableFuture<List<UOItem>> findAllGroundItems();

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    CompletableFuture<UOItem> findItemBySerialId(int serialId);

    CompletableFuture<UOItem> saveItemFull(UOItem item);

    CompletableFuture<Collection<UOItem>> saveItems(int serial, Collection<UOItem> items, Collection<UOItem> dirties);

    CompletableFuture<Collection<UOItem>> saveStates(Collection<UOItem> items);
}
