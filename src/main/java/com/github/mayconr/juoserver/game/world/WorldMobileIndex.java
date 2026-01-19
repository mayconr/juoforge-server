package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WorldMobileIndex {

    private final ConcurrentMap<Long, Set<Integer>> mobilesByRegion =
            new ConcurrentHashMap<>();

    public void add(UOMobile mobile) {
        long key = regionKey(mobile.getX() / 24, mobile.getY() / 24);
        mobilesByRegion
                .computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                .add(mobile.getSerialId());
    }

    public void remove(UOMobile mobile) {
        long key = regionKey(mobile.getX() / 24, mobile.getY() / 24);
        var set = mobilesByRegion.get(key);
        if (set != null) {
            set.remove(mobile.getSerialId());
        }
    }

    public List<Integer> getSerialsInRange(Location location) {
        int blockX = location.getX() / 24;
        int blockY = location.getY() / 24;

        List<Integer> result = new ArrayList<>(32);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                long key = regionKey(blockX + dx, blockY + dy);
                var set = mobilesByRegion.get(key);
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
