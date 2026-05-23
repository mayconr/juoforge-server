package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WorldMobileIndex {

    private static final int BLOCK_SIZE = 24;
    private final ConcurrentMap<Long, Set<Integer>> mobilesByRegion = new ConcurrentHashMap<>();

    public void addAll(Collection<UOMobile> mobiles) {
        for (UOMobile mobile : mobiles) {
            add(mobile);
        }
    }

    public void add(UOMobile mobile) {
        add(mobile, mobile);
    }

    public void add(UOMobile mobile, Location location) {
        long key = regionKey(location.getX() / 24, location.getY() / 24);
        mobilesByRegion
                .computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                .add(mobile.getSerialId());
    }

    public void remove(UOMobile mobile) {
        remove(mobile, mobile);
    }

    public void remove(UOMobile mobile, Location location) {
        long key = regionKey(location.getX() / 24, location.getY() / 24);
        var set = mobilesByRegion.get(key);
        if (set != null) {
            set.remove(mobile.getSerialId());
        }
    }

    public List<Integer> getNearbySerials(Location location, int radius) {
        int blockX = location.getX() / BLOCK_SIZE;
        int blockY = location.getY() / BLOCK_SIZE;

        int blockRadius = (radius / BLOCK_SIZE) + 1;

        List<Integer> result = new ArrayList<>(BLOCK_SIZE * 2);

        for (int dx = -blockRadius; dx <= blockRadius; dx++) {
            for (int dy = -blockRadius; dy <= blockRadius; dy++) {
                long key = regionKey(blockX + dx, blockY + dy);
                var set = mobilesByRegion.get(key);
                if (set != null && !set.isEmpty()) {
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
