package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorldSaveTest {
    private final CachedRealmStorage storage = spy(new CachedRealmStorage(null, null, null, null));

    @Test
    void waitsForAllSavesAndCoalescesOverlappingCalls() {
        var mobiles = new CompletableFuture<Collection<UOMobile>>();
        var items = new CompletableFuture<Collection<UOItem>>();
        var extra = new CompletableFuture<Void>();
        doReturn(mobiles).when(storage).saveMobiles();
        doReturn(items).when(storage).saveItems();
        var listener = mock(WorldSaveListener.class);
        when(listener.save()).thenReturn(extra);
        storage.addWorldSaveListener(listener);

        var result = storage.saveWorld();
        assertSame(result, storage.saveWorld());
        mobiles.complete(List.of());
        items.complete(List.of());
        assertFalse(result.isDone());
        extra.complete(null);
        result.join();
        verify(storage).saveMobiles();
        verify(storage).saveItems();
        verify(listener).save();
    }

    @Test
    void attemptsEverySaveEvenWhenOneThrowsAndReportsFailureAfterAllFinish() {
        doThrow(new IllegalStateException("Mobile save failed")).when(storage).saveMobiles();
        doReturn(CompletableFuture.completedFuture(List.of())).when(storage).saveItems();
        var failing = mock(WorldSaveListener.class);
        when(failing.save()).thenThrow(new IllegalStateException("Listener failed"));
        var extra = new CompletableFuture<Void>();
        var remaining = mock(WorldSaveListener.class);
        when(remaining.save()).thenReturn(extra);
        storage.addWorldSaveListener(failing);
        storage.addWorldSaveListener(remaining);

        var result = storage.saveWorld();
        verify(storage).saveItems();
        verify(remaining).save();
        assertFalse(result.isDone());
        extra.complete(null);
        assertThrows(CompletionException.class, result::join);
    }

    @Test
    void newListenersParticipateOnlyInSubsequentSaves() {
        var mobiles = new CompletableFuture<Collection<UOMobile>>();
        doReturn(mobiles).when(storage).saveMobiles();
        doReturn(CompletableFuture.completedFuture(List.of())).when(storage).saveItems();
        var result = storage.saveWorld();
        var listener = mock(WorldSaveListener.class);
        when(listener.save()).thenReturn(CompletableFuture.completedFuture(null));
        storage.addWorldSaveListener(listener);
        mobiles.complete(List.of());
        result.join();
        verifyNoInteractions(listener);
        storage.saveWorld().join();
        verify(listener).save();
    }

    @Test
    void rejectsNullListenerAndNullFutureDoesNotSkipOtherParticipants() {
        assertThrows(NullPointerException.class, () -> storage.addWorldSaveListener(null));
        doReturn(CompletableFuture.completedFuture(List.of())).when(storage).saveMobiles();
        doReturn(CompletableFuture.completedFuture(List.of())).when(storage).saveItems();
        storage.addWorldSaveListener(() -> null);
        var listener = mock(WorldSaveListener.class);
        when(listener.save()).thenReturn(CompletableFuture.completedFuture(null));
        storage.addWorldSaveListener(listener);
        assertThrows(CompletionException.class, () -> storage.saveWorld().join());
        verify(listener).save();
    }
}
