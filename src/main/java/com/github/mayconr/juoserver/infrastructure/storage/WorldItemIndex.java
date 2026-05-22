package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WorldItemIndex {
    private final ConcurrentMap<Long, Set<Integer>> itemsByRegion = new ConcurrentHashMap<>();

    public void addAll(Collection<UOItem> items) {
        for (UOItem item : items) {
            add(item);
        }
    }

    public void add(UOItem item) {
        // TODO verify if item is on the ground
        long key = regionKey(item.getX() / 24, item.getY() / 24);
        itemsByRegion
                .computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                .add(item.getSerialId());
    }

    public void remove(UOItem item) {
        long key = regionKey(item.getX() / 24, item.getY() / 24);
        var set = itemsByRegion.get(key);
        if (set != null) {
            set.remove(item.getSerialId());
        }
    }

    public List<Integer> getSerialsInRange(Location location) {
        int blockX = location.getX() / 24;
        int blockY = location.getY() / 24;

        List<Integer> result = new ArrayList<>(64);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                long key = regionKey(blockX + dx, blockY + dy);
                var set = itemsByRegion.get(key);
                if (set != null) {
                    result.addAll(set);
                }
            }
        }
        return result;
    }

    private long regionKey(int x, int y) {
        return (((long) x) << 32) | (y & 0xFFFFFFFFL);
    }
}
