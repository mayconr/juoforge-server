package com.github.mayconr.juoserver.infrastructure.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentStorageTest {
    private final ObjectMapper json = new ObjectMapper();
    private final DocumentStorage backend = mock(DocumentStorage.class);

    private CachedRealmStorage realm() {
        var realm = spy(new CachedRealmStorage(null, null, null, null, backend));
        doReturn(CompletableFuture.completedFuture(List.of())).when(realm).saveMobiles();
        doReturn(CompletableFuture.completedFuture(List.of())).when(realm).saveItems();
        return realm;
    }

    @Test
    void cacheAndLookupsUseDefensiveCopiesAndPerformNoDatabaseAccess() {
        var realm = realm();
        var document = json.createObjectNode().put("consumed", 2);
        realm.cacheDocument("mining.veins", document);
        document.put("consumed", 99);
        var copy = realm.getDocument("mining.veins").orElseThrow();
        ((com.fasterxml.jackson.databind.node.ObjectNode) copy).put("consumed", 88);
        assertEquals(2, realm.getDocument("mining.veins").orElseThrow().get("consumed").asInt());
        assertTrue(realm.getDocument("missing").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> realm.cacheDocument(" ", document));
        assertThrows(IllegalArgumentException.class, () -> realm.getDocument("x".repeat(101)));
        verifyNoInteractions(backend);
    }

    @Test
    void initializeLoadsAllDocumentsBeforeReturningAndBeforeCallback() {
        var mobiles = mock(MobileStorage.class);
        var items = mock(ItemStorage.class);
        when(mobiles.findAllNpcs()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.findAllGroundItems()).thenReturn(CompletableFuture.completedFuture(List.of()));
        var restored = json.createObjectNode().put("consumed", 10);
        when(backend.loadAll()).thenReturn(CompletableFuture.completedFuture(Map.of("mining.veins", restored)));
        var realm = new CachedRealmStorage(mobiles, items, null, new ItemMapper(null), backend);
        var callback = new AtomicBoolean();
        realm.initialize(() -> 1, () -> 1, data -> {
            assertEquals(restored, realm.getDocument("mining.veins").orElseThrow());
            callback.set(true);
        });
        assertTrue(callback.get());
        restored.put("consumed", 99);
        assertEquals(10, realm.getDocument("mining.veins").orElseThrow().get("consumed").asInt());
        verify(backend).loadAll();
        verifyNoMoreInteractions(backend);
    }

    @Test
    void failedDocumentLoadingAbortsInitialization() {
        when(backend.loadAll()).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("offline")));
        var realm = new CachedRealmStorage(null, null, null, null, backend);
        assertThrows(CompletionException.class, () -> realm.initialize(() -> 1, () -> 1, data -> fail()));
    }

    @Test
    void worldSavePersistsAnIsolatedSnapshotAndWaitsForDatabase() {
        var realm = realm();
        realm.cacheDocument("mining.veins", json.createArrayNode().add(2));
        var write = new CompletableFuture<Void>();
        when(backend.saveAll(any())).thenReturn(write);
        var completion = realm.saveWorld();
        assertSame(completion, realm.saveWorld());
        verify(backend).saveAll(Map.of("mining.veins", json.createArrayNode().add(2)));
        assertFalse(completion.isDone());
        realm.cacheDocument("mining.veins", json.createArrayNode().add(4));
        // Changes during persistence are included only in the next save.
        verify(backend).saveAll(Map.of("mining.veins", json.createArrayNode().add(2)));
        write.complete(null);
        completion.join();
        realm.saveWorld().join();
        verify(backend).saveAll(Map.of("mining.veins", json.createArrayNode().add(4)));
    }

    @Test
    void failedWritesKeepTheCacheForRetry() {
        var realm = realm();
        realm.cacheDocument("test", json.createArrayNode());
        when(backend.saveAll(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("offline")))
                .thenReturn(CompletableFuture.completedFuture(null));
        assertThrows(CompletionException.class, () -> realm.saveWorld().join());
        realm.saveWorld().join();
        verify(backend, times(2)).saveAll(Map.of("test", json.createArrayNode()));
    }

}
