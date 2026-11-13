package com.github.mayconr.juoserver.infrastructure.storage.item;

import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.infrastructure.storage.CachedWorldStorage;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundItemsQueryTest {
    @Test
    void viewFindsGroundItemsInAllRegionsAndDiscardsRelocatedOrDeletedEntries() {
        var storage = new CachedWorldStorage(null, null, null, null);
        var near = item(1, 100);
        var far = item(2, 5000);
        storage.items().cache(near);
        storage.items().cache(far);
        assertEquals(2, storage.items().onGround().size());

        near.setCurrentLocation(ItemLocation.container(0x40000003));
        assertEquals(List.of(far), storage.items().onGround());
        storage.items().delete(far);
        assertTrue(storage.items().onGround().isEmpty());
    }

    @Test
    void indexRejectsNonGroundItemsAndSnapshotDeduplicatesStaleRegionEntries() {
        var cache = new ItemCache();
        var index = new WorldItemIndex();
        var query = new GroundItemsQuery(cache, index);
        var item = item(1, 100);
        item.setCurrentLocation(ItemLocation.orphan());
        cache.put(item);
        index.add(item);
        assertTrue(query.get().isEmpty());

        item.setCurrentLocation(ItemLocation.ground());
        index.add(item);
        item.setX(5000);
        index.add(item);
        assertEquals(List.of(item), query.get());
        cache.remove(item);
        assertTrue(query.get().isEmpty());
    }

    private static UOItem item(int serial, int x) {
        var data = new UOItemData();
        data.setSerialId(0x40000000 + serial);
        data.setLocationType(ItemLocationType.GROUND);
        data.setX(x);
        return new UOItem(data, null);
    }
}
