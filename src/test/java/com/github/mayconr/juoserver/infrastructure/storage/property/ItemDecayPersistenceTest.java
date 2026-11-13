package com.github.mayconr.juoserver.infrastructure.storage.property;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.decay.ItemDecayClock;
import com.github.mayconr.juoserver.game.item.ItemModuleImpl;
import com.github.mayconr.juoserver.game.item.decay.ItemDecayState;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemDecayPersistenceTest {
    @Test
    void dropPropertyAndWorldClockRoundTripThroughJsonWithoutAddingOfflineTime() throws Exception {
        var json = new ObjectMapper();
        var database = new HashMap<String, JsonNode>();
        var backend = mock(PropertyStorage.class);
        when(backend.loadAll()).thenAnswer(call -> CompletableFuture.completedFuture(Map.copyOf(database)));
        when(backend.saveAll(anyMap(), anySet())).thenAnswer(call -> {
            Map<String, JsonNode> upserts = call.getArgument(0);
            Set<String> deletes = call.getArgument(1);
            database.putAll(upserts);
            deletes.forEach(database::remove);
            return CompletableFuture.completedFuture(null);
        });

        var firstProperties = new PropertyStore(backend);
        var clock = new ItemDecayClock(firstProperties);
        var data = new UOItemData();
        data.setLocationType(ItemLocationType.GROUND);
        data.setPersistentAttrMap(new DefaultAttributeMap());
        var item = new UOItem(data, null);
        var items = new ItemModuleImpl(null, null, null);
        ItemDecayState.recordDrop(items, item, clock.now());
        long droppedAt = ItemDecayState.droppedAt(items, item, -1);
        clock.advance(60);
        firstProperties.save().join();
        var savedItemProps = json.writeValueAsString(item.persistentAttributes().toMap());

        var restoredClock = new ItemDecayClock(new PropertyStore(backend));
        var restoredData = new UOItemData();
        restoredData.setLocationType(ItemLocationType.GROUND);
        Map<String, Object> attributes = json.readValue(savedItemProps, new TypeReference<>() { });
        restoredData.setPersistentAttrMap(new DefaultAttributeMap(attributes));
        var restoredItem = new UOItem(restoredData, null);
        assertEquals(droppedAt, ItemDecayState.droppedAt(items, restoredItem, -1));
        assertEquals(clock.now(), restoredClock.now());
        assertFalse(ItemDecayState.isDue(items, restoredItem, restoredClock.now(), 120));
        restoredClock.advance(60);
        assertTrue(ItemDecayState.isDue(items, restoredItem, restoredClock.now(), 120));
    }
}
