package com.github.mayconr.juoserver.game.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.mayconr.juoserver.game.model.UOItem;

public class ItemCache {
    private final ConcurrentMap<Integer, UOItem> items = new ConcurrentHashMap<>();

    public UOItem get(int serialId) {
        return items.get(serialId);
    }

    public UOItem put(UOItem item) {
        items.put(item.getSerialId(), item);
        return item;
    }

    public List<UOItem> putAll(Collection<UOItem> loadedItems) {
        List<UOItem> result = new ArrayList<>(loadedItems.size());

        for (UOItem item : loadedItems) {
            result.add(getOrPut(item));
        }

        return result;
    }

    private UOItem getOrPut(UOItem item) {
        var cached = items.putIfAbsent(item.getSerialId(), item);
        return cached != null ? cached : item;
    }
}
