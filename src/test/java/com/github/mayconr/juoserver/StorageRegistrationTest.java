package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.Storage;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageRegistrationTest {
    @Test
    void createsListenersWithRuntimeAndRegistersThemWithoutSaving() {
        var cfg = new DefaultWorldCfg();
        var runtime = mock(ServerRuntime.class);
        var storage = mock(RealmStorage.class);
        when(runtime.storage()).thenReturn(storage);
        var first = mock(Storage.class);
        var second = mock(Storage.class);
        var factoryCalls = new AtomicInteger();
        cfg.registerStorage(actual -> {
            assertSame(runtime, actual);
            factoryCalls.incrementAndGet();
            return first;
        });
        cfg.registerStorage(actual -> second);
        assertEquals(0, factoryCalls.get());

        WorldBootstrap.registerStorages(cfg, runtime);

        assertEquals(1, factoryCalls.get());
        var order = inOrder(storage);
        order.verify(storage).registerStorage(first);
        order.verify(storage).registerStorage(second);
        verifyNoInteractions(first, second);
        verify(storage, never()).saveWorld();
    }

    @Test
    void returnsImmutableSnapshotAndRejectsNullFactories() {
        var cfg = new DefaultWorldCfg();
        var snapshot = cfg.storageList();
        cfg.registerStorage(runtime -> () -> CompletableFuture.completedFuture(null));
        assertTrue(snapshot.isEmpty());
        assertEquals(1, cfg.storageList().size());
        assertThrows(UnsupportedOperationException.class, () -> cfg.storageList().clear());
        assertThrows(NullPointerException.class, () -> cfg.registerStorage(null));
    }

    @Test
    void rejectsFactoryReturningNull() {
        var cfg = new DefaultWorldCfg();
        cfg.registerStorage(runtime -> null);
        assertThrows(NullPointerException.class,
                () -> WorldBootstrap.registerStorages(cfg, mock(ServerRuntime.class)));
    }
}
