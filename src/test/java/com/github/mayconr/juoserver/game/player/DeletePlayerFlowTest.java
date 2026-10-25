package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.flow.deletion.*;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeletePlayerFlowTest {
    private final RealmStorage storage = mock(RealmStorage.class);

    private PlayerModule module() {
        var registry = new DefaultFlowRegistry();
        registry.register("delete-player", DeletePlayerFlowDefinition.build(storage), DeletePlayerContext.class);
        var module = new PlayerModuleImpl(mock(PlayerVitalsHandler.class), storage, mock(EventBus.class));
        module.initialize(DefaultModuleContext.builder()
                .flowFacade(DefaultFlowFacade.builder().registry(registry).build()).build());
        return module;
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 0x40000001, Integer.MAX_VALUE})
    void rejectsInvalidSerialBeforeAccessingStorage(int serial) {
        var result = module().deletePlayerMobile(serial);

        assertInstanceOf(IllegalArgumentException.class,
                assertThrows(CompletionException.class, result::join).getCause());
        verifyNoInteractions(storage);
    }

    @Test
    void rejectsLoadedMobileWithoutDeletingIt() {
        when(storage.getMobile(42)).thenReturn(Optional.of(mock(UOPlayer.class)));

        var result = module().deletePlayerMobile(42);

        assertInstanceOf(IllegalStateException.class,
                assertThrows(CompletionException.class, result::join).getCause());
        verify(storage, never()).deleteMobile(anyInt());
    }

    @Test
    void completesOnlyAfterAsynchronousDeletionFinishes() {
        when(storage.getMobile(42)).thenReturn(Optional.empty());
        var deletion = new CompletableFuture<Void>();
        when(storage.deleteMobile(42)).thenReturn(deletion);

        var result = module().deletePlayerMobile(42);

        assertFalse(result.isDone());
        deletion.complete(null);
        assertNull(result.join());
        verify(storage).deleteMobile(42);
    }

    @Test
    void propagatesAsynchronousStorageFailure() {
        var failure = new IllegalStateException("Database unavailable");
        when(storage.deleteMobile(42)).thenReturn(CompletableFuture.failedFuture(failure));

        var result = module().deletePlayerMobile(42);

        assertSame(failure, assertThrows(CompletionException.class, result::join).getCause());
    }

    @Test
    void synchronousStorageFailureDoesNotLeaveResultPending() {
        when(storage.deleteMobile(42)).thenThrow(new IllegalStateException("Storage failed"));

        var result = module().deletePlayerMobile(42);

        assertTrue(result.isCompletedExceptionally());
    }
}
