package com.github.mayconr.juoserver.infrastructure.storage.item;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ItemStorage {

    CompletableFuture<List<UOItem>> loadEquippedItems(UOMobile mobile);

    CompletableFuture<Optional<UOItem>> findItemBySerialId(int serialId);

}
