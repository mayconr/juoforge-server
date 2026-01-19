package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.UOItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    public Collection<UOItem> getItems() {
        return items.values();
    }

    public void remove(UOItem item) {
        items.remove(item.getSerialId());
    }
}
