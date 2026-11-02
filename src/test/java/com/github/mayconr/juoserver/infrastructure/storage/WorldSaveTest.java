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
    void individualSavesDoNotInvokeRegisteredStorages() {
        var mobiles = mock(MobileStorage.class);
        var items = mock(ItemStorage.class);
        when(mobiles.findAllNpcs()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.findAllGroundItems()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(mobiles.saveMobiles(anyInt(), anyCollection(), anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.saveItems(anyInt(), anyCollection(), anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));
        var realm = new CachedRealmStorage(mobiles, items, null, new ItemMapper(null));
        realm.initialize(() -> 1, () -> 1, data -> {});
        var participant = mock(Storage.class);
        when(participant.save()).thenReturn(CompletableFuture.completedFuture(null));
        realm.registerStorage(participant);

        realm.saveMobiles().join();
        realm.saveItems().join();
        verifyNoInteractions(participant);

        realm.saveWorld().join();
        verify(participant).save();
    }

    @Test
    void waitsForAllSavesAndCoalescesOverlappingCalls() {
        var mobiles = new CompletableFuture<Collection<UOMobile>>();
        var items = new CompletableFuture<Collection<UOItem>>();
        var extra = new CompletableFuture<Void>();
        doReturn(mobiles).when(storage).saveMobiles();
        doReturn(items).when(storage).saveItems();
        var listener = mock(Storage.class);
        when(listener.save()).thenReturn(extra);
        storage.registerStorage(listener);

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
        var failing = mock(Storage.class);
        when(failing.save()).thenThrow(new IllegalStateException("Listener failed"));
        var extra = new CompletableFuture<Void>();
        var remaining = mock(Storage.class);
        when(remaining.save()).thenReturn(extra);
        storage.registerStorage(failing);
        storage.registerStorage(remaining);

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
        var listener = mock(Storage.class);
        when(listener.save()).thenReturn(CompletableFuture.completedFuture(null));
        storage.registerStorage(listener);
        mobiles.complete(List.of());
        result.join();
        verifyNoInteractions(listener);
        storage.saveWorld().join();
        verify(listener).save();
    }

    @Test
    void rejectsNullListenerAndNullFutureDoesNotSkipOtherParticipants() {
        assertThrows(NullPointerException.class, () -> storage.registerStorage(null));
        doReturn(CompletableFuture.completedFuture(List.of())).when(storage).saveMobiles();
        doReturn(CompletableFuture.completedFuture(List.of())).when(storage).saveItems();
        storage.registerStorage(() -> null);
        var listener = mock(Storage.class);
        when(listener.save()).thenReturn(CompletableFuture.completedFuture(null));
        storage.registerStorage(listener);
        assertThrows(CompletionException.class, () -> storage.saveWorld().join());
        verify(listener).save();
    }
}
