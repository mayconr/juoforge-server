package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MobileCache {

    private final ConcurrentMap<Integer, UOMobile> mobiles = new ConcurrentHashMap<>();

    public UOMobile get(int serialId) {
        return mobiles.get(serialId);
    }

    public void putAll(Collection<UOMobile> mobiles) {
        for (UOMobile mobile : mobiles) {
            put(mobile);
        }
    }

    public UOMobile put(UOMobile mobile) {
        mobiles.put(mobile.getSerialId(), mobile);
        return mobile;
    }

    public void remove(UOMobile mobile) {
        mobiles.remove(mobile.getSerialId());
    }

    public Collection<UOMobile> getMobiles() {
        return mobiles.values();
    }
}
