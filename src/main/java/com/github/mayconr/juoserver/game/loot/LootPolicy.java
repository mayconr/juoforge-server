package com.github.mayconr.juoserver.game.loot;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import java.util.List;

/** Shard extension point: resolves requests without creating or placing items. */
@FunctionalInterface
public interface LootPolicy {
    List<ItemRequest> resolve(LootRequest request, RNG rng);

    static LootPolicy none() {
        return (request, rng) -> List.of();
    }
}
