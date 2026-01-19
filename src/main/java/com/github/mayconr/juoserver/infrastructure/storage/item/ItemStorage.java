package com.github.mayconr.juoserver.infrastructure.storage.item;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ItemStorage {

    CompletableFuture<List<UOItem>> loadEquippedItems(UOMobile mobile);

    CompletableFuture<List<UOItem>> loadGroundItems();

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    CompletableFuture<Optional<UOItem>> findItemBySerialId(int serialId);

    CompletableFuture<UOItem> saveItemFull(UOItem item);

    CompletableFuture<Collection<UOItem>> saveStates(Collection<UOItem> items);
}
