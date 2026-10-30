package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.WorldSaveListener;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorldSaveListenerRegistrationTest {
    @Test
    void createsListenersWithRuntimeAndRegistersThemWithoutSaving() {
        var cfg = new DefaultWorldCfg();
        var runtime = mock(ServerRuntime.class);
        var storage = mock(RealmStorage.class);
        when(runtime.storage()).thenReturn(storage);
        var first = mock(WorldSaveListener.class);
        var second = mock(WorldSaveListener.class);
        var factoryCalls = new AtomicInteger();
        cfg.addWorldSaveListener(actual -> {
            assertSame(runtime, actual);
            factoryCalls.incrementAndGet();
            return first;
        });
        cfg.addWorldSaveListener(actual -> second);
        assertEquals(0, factoryCalls.get());

        WorldBootstrap.registerWorldSaveListeners(cfg, runtime);

        assertEquals(1, factoryCalls.get());
        var order = inOrder(storage);
        order.verify(storage).addWorldSaveListener(first);
        order.verify(storage).addWorldSaveListener(second);
        verifyNoInteractions(first, second);
        verify(storage, never()).saveWorld();
    }

    @Test
    void returnsImmutableSnapshotAndRejectsNullFactories() {
        var cfg = new DefaultWorldCfg();
        var snapshot = cfg.worldSaveListenerList();
        cfg.addWorldSaveListener(runtime -> () -> CompletableFuture.completedFuture(null));
        assertTrue(snapshot.isEmpty());
        assertEquals(1, cfg.worldSaveListenerList().size());
        assertThrows(UnsupportedOperationException.class, () -> cfg.worldSaveListenerList().clear());
        assertThrows(NullPointerException.class, () -> cfg.addWorldSaveListener(null));
    }

    @Test
    void rejectsFactoryReturningNull() {
        var cfg = new DefaultWorldCfg();
        cfg.addWorldSaveListener(runtime -> null);
        assertThrows(NullPointerException.class,
                () -> WorldBootstrap.registerWorldSaveListeners(cfg, mock(ServerRuntime.class)));
    }
}
