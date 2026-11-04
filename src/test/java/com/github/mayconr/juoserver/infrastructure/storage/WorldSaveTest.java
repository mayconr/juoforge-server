package com.github.mayconr.juoserver.infrastructure.storage;

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
    private final DocumentStorage documents = mock(DocumentStorage.class);
    private final CachedRealmStorage storage = spy(new CachedRealmStorage(null, null, null, null, documents));

    @Test
    void waitsForAllSavesAndCoalescesOverlappingCalls() {
        var mobiles = new CompletableFuture<Collection<UOMobile>>();
        var items = new CompletableFuture<Collection<UOItem>>();
        var documentWrite = new CompletableFuture<Void>();
        doReturn(mobiles).when(storage).saveMobiles();
        doReturn(items).when(storage).saveItems();
        when(documents.saveAll(Map.of())).thenReturn(documentWrite);
        var result = storage.saveWorld();
        assertSame(result, storage.saveWorld());
        mobiles.complete(List.of());
        items.complete(List.of());
        assertFalse(result.isDone());
        documentWrite.complete(null);
        result.join();
        verify(storage).saveMobiles();
        verify(storage).saveItems();
        verify(documents).saveAll(Map.of());
    }

    @Test
    void attemptsEverySaveEvenWhenOneThrowsAndReportsFailureAfterAllFinish() {
        doThrow(new IllegalStateException("Mobile save failed")).when(storage).saveMobiles();
        doReturn(CompletableFuture.completedFuture(List.of())).when(storage).saveItems();
        var pending = new CompletableFuture<Void>();
        when(documents.saveAll(Map.of())).thenReturn(pending);
        var result = storage.saveWorld();
        assertFalse(result.isDone());
        pending.complete(null);
        assertThrows(CompletionException.class, result::join);
        verify(storage).saveItems();
        verify(documents).saveAll(Map.of());
    }

    @Test
    void individualSavesDoNotPersistDocuments() {
        var mobiles = mock(MobileStorage.class);
        var items = mock(ItemStorage.class);
        when(mobiles.findAllNpcs()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.findAllGroundItems()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(documents.loadAll()).thenReturn(CompletableFuture.completedFuture(Map.of()));
        when(mobiles.saveMobiles(anyInt(), anyCollection(), anyCollection())).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.saveItems(anyInt(), anyCollection(), anyCollection())).thenReturn(CompletableFuture.completedFuture(List.of()));
        var realm = new CachedRealmStorage(mobiles, items, null, new ItemMapper(null), documents);
        realm.initialize(() -> 1, () -> 1, data -> {});
        realm.saveMobiles().join();
        realm.saveItems().join();
        verify(documents, never()).saveAll(any());
    }
}
