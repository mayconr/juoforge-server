package com.github.mayconr.juoserver.infrastructure.storage.property;

import com.github.mayconr.juoserver.infrastructure.storage.CachedWorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemMapper;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileStorage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.infrastructure.serialization.DataType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PropertiesTest {
    private static final WorldKey<Counter> COUNTER = WorldKey.of("test.counter", Counter.class);
    private static final WorldKey<List<Counter>> COUNTERS = WorldKey.of("test.counters", new DataType<>() {});

    private final ObjectMapper json = new ObjectMapper();
    private final PropertyStorage backend = mock(PropertyStorage.class);
    private boolean loadStubbed;

    record Counter(String id, int value) {}

    @Test
    void storesTypedValuesInMemoryWithoutDatabaseWrites() {
        var properties = loadedWorldStorage().properties();

        properties.put(COUNTER, new Counter("a", 2));
        properties.put(COUNTERS, List.of(new Counter("b", 3)));

        assertEquals(new Counter("a", 2), properties.get(COUNTER).orElseThrow());
        assertEquals(List.of(new Counter("b", 3)), properties.get(COUNTERS).orElseThrow());
        assertTrue(properties.get(WorldKey.of("missing", Counter.class)).isEmpty());
        verify(backend).loadAll();
        verifyNoMoreInteractions(backend);
    }

    @Test
    void valuesAreCopiedOnWriteAndRead() {
        var properties = loadedWorldStorage().properties();
        var written = new ArrayList<>(List.of(new Counter("a", 1)));

        properties.put(COUNTERS, written);
        written.add(new Counter("b", 2));
        properties.get(COUNTERS).orElseThrow().clear();

        assertEquals(List.of(new Counter("a", 1)), properties.get(COUNTERS).orElseThrow());
    }

    @Test
    void rejectsInvalidNamesAndMissingValues() {
        var properties = loadedWorldStorage().properties();

        assertThrows(IllegalArgumentException.class, () -> WorldKey.of(" ", Counter.class));
        assertThrows(IllegalArgumentException.class, () -> WorldKey.of("x".repeat(101), Counter.class));
        assertThrows(NullPointerException.class, () -> properties.put(COUNTER, null));
    }

    @Test
    void initializeLoadsPropertiesBeforeCallback() {
        stubLoad(Map.of("test.counter", tree(new Counter("a", 10))));
        var worldStorage = worldStorageWithAdapters();
        var callback = new AtomicBoolean();

        worldStorage.initialize(() -> 1, () -> 1, data -> {
            assertEquals(new Counter("a", 10), worldStorage.properties().get(COUNTER).orElseThrow());
            callback.set(true);
        });

        assertTrue(callback.get());
        verify(backend).loadAll();
    }

    @Test
    void firstAccessBeforeInitializationLoadsOnlyOnce() {
        stubLoad(Map.of("test.counter", tree(new Counter("a", 10))));
        var worldStorage = worldStorageWithAdapters();

        assertEquals(new Counter("a", 10), worldStorage.properties().get(COUNTER).orElseThrow());
        worldStorage.initialize(() -> 1, () -> 1, data -> {});

        verify(backend, times(1)).loadAll();
    }

    @Test
    void failedLoadAbortsInitialization() {
        when(backend.loadAll()).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("offline")));
        var worldStorage = new CachedWorldStorage(null, null, null, null, backend);

        assertThrows(CompletionException.class, () -> worldStorage.initialize(() -> 1, () -> 1, data -> fail()));
    }

    @Test
    void ignoresUnknownFieldsAndRejectsInvalidStoredValues() {
        var stored = json.createObjectNode().put("id", "a").put("value", 4).put("removed", true);
        stubLoad(Map.of("test.counter", stored, "test.counters", json.createObjectNode()));
        var properties = new CachedWorldStorage(null, null, null, null, backend).properties();

        assertEquals(new Counter("a", 4), properties.get(COUNTER).orElseThrow());
        assertThrows(IllegalStateException.class, () -> properties.get(COUNTERS));
    }

    @Test
    void worldSaveWritesOnlyChangedProperties() {
        stubLoad(Map.of("test.counter", tree(new Counter("a", 1))));
        var worldStorage = loadedWorldStorage();
        stubSave(CompletableFuture.completedFuture(null));

        worldStorage.saveWorld().join();
        worldStorage.properties().put(COUNTERS, List.of(new Counter("b", 2)));
        worldStorage.properties().put(COUNTERS, List.of(new Counter("b", 3)));
        worldStorage.saveWorld().join();
        worldStorage.saveWorld().join();

        verify(backend, times(1)).saveAll(anyMap(), anySet());
        verify(backend).saveAll(Map.of("test.counters", tree(List.of(new Counter("b", 3)))), Set.of());
    }

    @Test
    void changesDuringAWriteRemainPendingForTheNextSave() {
        var worldStorage = loadedWorldStorage();
        var write = new CompletableFuture<Void>();
        stubSave(write);
        worldStorage.properties().put(COUNTER, new Counter("a", 1));

        var completion = worldStorage.saveWorld();
        worldStorage.properties().put(COUNTER, new Counter("a", 2));
        write.complete(null);
        completion.join();
        stubSave(CompletableFuture.completedFuture(null));
        worldStorage.saveWorld().join();

        verify(backend).saveAll(Map.of("test.counter", tree(new Counter("a", 1))), Set.of());
        verify(backend).saveAll(Map.of("test.counter", tree(new Counter("a", 2))), Set.of());
    }

    @Test
    void failedWritesAreRetriedByTheNextSave() {
        var worldStorage = loadedWorldStorage();
        worldStorage.properties().put(COUNTER, new Counter("a", 1));
        when(backend.saveAll(anyMap(), anySet()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("offline")))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertThrows(CompletionException.class, () -> worldStorage.saveWorld().join());
        worldStorage.saveWorld().join();

        verify(backend, times(2)).saveAll(Map.of("test.counter", tree(new Counter("a", 1))), Set.of());
    }

    @Test
    void removalsArePersistedAsDeletions() {
        stubLoad(Map.of("test.counter", tree(new Counter("a", 1))));
        var worldStorage = loadedWorldStorage();
        stubSave(CompletableFuture.completedFuture(null));

        worldStorage.properties().remove(COUNTER);
        worldStorage.properties().remove(WorldKey.of("never.stored", Counter.class));

        assertTrue(worldStorage.properties().get(COUNTER).isEmpty());
        worldStorage.saveWorld().join();
        worldStorage.saveWorld().join();
        verify(backend, times(1)).saveAll(anyMap(), anySet());
        verify(backend).saveAll(Map.of(), Set.of("test.counter"));
    }

    @Test
    void savingChangesWithoutConfiguredStorageFails() {
        var worldStorage = spy(new CachedWorldStorage(null, null, null, null, null));
        stubWorldSaves(worldStorage);

        worldStorage.saveWorld().join();
        worldStorage.properties().put(COUNTER, new Counter("a", 1));

        assertThrows(CompletionException.class, () -> worldStorage.saveWorld().join());
    }

    @Test
    void individualSavesDoNotPersistProperties() {
        var worldStorage = worldStorageWithAdapters();
        worldStorage.initialize(() -> 1, () -> 1, data -> {});
        worldStorage.properties().put(COUNTER, new Counter("a", 1));

        worldStorage.saveMobiles().join();
        worldStorage.saveItems().join();

        verify(backend, never()).saveAll(any(), any());
    }

    private CachedWorldStorage loadedWorldStorage() {
        if (!loadStubbed) {
            stubLoad(Map.of());
        }
        var worldStorage = spy(new CachedWorldStorage(null, null, null, null, backend));
        stubWorldSaves(worldStorage);
        worldStorage.properties().get(COUNTER);
        return worldStorage;
    }

    private CachedWorldStorage worldStorageWithAdapters() {
        if (!loadStubbed) {
            stubLoad(Map.of());
        }
        var mobiles = mock(MobileStorage.class);
        var items = mock(ItemStorage.class);
        when(mobiles.findAllNpcs()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.findAllGroundItems()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(mobiles.saveMobiles(anyInt(), anyCollection(), anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.saveItems(anyInt(), anyCollection(), anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));
        return new CachedWorldStorage(mobiles, items, null, new ItemMapper(null), backend);
    }

    private static void stubWorldSaves(CachedWorldStorage worldStorage) {
        doReturn(CompletableFuture.completedFuture(List.of())).when(worldStorage).saveMobiles();
        doReturn(CompletableFuture.completedFuture(List.of())).when(worldStorage).saveItems();
    }

    private void stubLoad(Map<String, JsonNode> stored) {
        loadStubbed = true;
        when(backend.loadAll()).thenReturn(CompletableFuture.completedFuture(stored));
    }

    private void stubSave(CompletableFuture<Void> result) {
        when(backend.saveAll(anyMap(), anySet())).thenReturn(result);
    }

    private JsonNode tree(Object value) {
        return json.valueToTree(value);
    }
}
