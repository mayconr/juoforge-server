package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.infrastructure.storage.property.PropertyStorage;
import com.github.mayconr.juoserver.infrastructure.storage.property.WorldKey;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import org.junit.jupiter.api.Test;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorldSaveTest {
    private static final WorldKey<Integer> KEY = WorldKey.of("test.value", Integer.class);

    private final PropertyStorage properties = mock(PropertyStorage.class);
    private final CachedWorldStorage storage = spy(new CachedWorldStorage(null, null, null, null, properties));

    WorldSaveTest() {
        when(properties.loadAll()).thenReturn(CompletableFuture.completedFuture(Map.of()));
        storage.properties().put(KEY, 1);
    }

    @Test
    void waitsForAllSavesAndCoalescesOverlappingCalls() {
        var mobiles = new CompletableFuture<Collection<UOMobile>>();
        var items = new CompletableFuture<Collection<UOItem>>();
        var propertyWrite = new CompletableFuture<Void>();
        doReturn(mobiles).when(storage).saveMobiles();
        doReturn(items).when(storage).saveItems();
        when(properties.saveAll(anyMap(), anySet())).thenReturn(propertyWrite);
        var result = storage.saveWorld();
        assertSame(result, storage.saveWorld());
        mobiles.complete(List.of());
        items.complete(List.of());
        assertFalse(result.isDone());
        propertyWrite.complete(null);
        result.join();
        verify(storage).saveMobiles();
        verify(storage).saveItems();
        verify(properties).saveAll(anyMap(), anySet());
    }

    @Test
    void attemptsEverySaveEvenWhenOneThrowsAndReportsFailureAfterAllFinish() {
        doThrow(new IllegalStateException("Mobile save failed")).when(storage).saveMobiles();
        doReturn(CompletableFuture.completedFuture(List.of())).when(storage).saveItems();
        var pending = new CompletableFuture<Void>();
        when(properties.saveAll(anyMap(), anySet())).thenReturn(pending);
        var result = storage.saveWorld();
        assertFalse(result.isDone());
        pending.complete(null);
        assertThrows(CompletionException.class, result::join);
        verify(storage).saveItems();
        verify(properties).saveAll(anyMap(), anySet());
    }
}
