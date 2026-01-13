package com.github.mayconr.juoserver.game.storage.item;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.github.mayconr.juoserver.game.core.model.UOItem;
import com.github.mayconr.juoserver.game.core.model.UOMobile;

public interface ItemStorage {

    CompletableFuture<List<UOItem>> loadEquippedItems(UOMobile mobile);

    CompletableFuture<Optional<UOItem>> findItemBySerialId(int serialId);
}
