package com.github.mayconr.juoserver.infrastructure.storage.item;

import com.github.mayconr.juoserver.game.model.UOItem;
import java.util.List;
import java.util.Objects;

/** Resolves the ground index snapshot, discarding removed or relocated entries. */
public record GroundItemsQuery(ItemCache cache, WorldItemIndex index) {
    public List<UOItem> get() {
        return index.groundSerials().stream()
                .map(cache::get)
                .filter(Objects::nonNull)
                .filter(UOItem::isOnTheGround)
                .toList();
    }
}
