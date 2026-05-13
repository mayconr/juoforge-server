package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ItemStorage {

    // ====== Item serial manipulation ============

    CompletableFuture<Integer> findNextItemSerial();

    // ====== Item queries ============

    CompletableFuture<List<UOItemData>> findAllEquippedItems(int mobileSerialId);

    CompletableFuture<List<UOItemData>> findAllGroundItems();

    CompletableFuture<List<UOItemData>> loadContainerItems(int containerSerialId);

    CompletableFuture<UOItemData> findItemBySerialId(int itemSerialId);

    // ====== Item updates ============

    CompletableFuture<UOItem> saveItemFull(UOItem item);

    CompletableFuture<Collection<UOItemData>> saveItems(int currentSerialId, Collection<UOItemData> items, Collection<UOItemData> dirties);

}
