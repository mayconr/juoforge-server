package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerModuleTest {
    @Test
    void onlinePlayersReflectSessionsAndReturnIndependentSnapshot() {
        var storage = mock(RealmStorage.class);
        var module = new PlayerModule(mock(PlayerVitalsHandler.class), storage, mock(EventBus.class));
        WorldPlayer players = module;
        var player = mock(UOPlayer.class);
        when(player.getSerialId()).thenReturn(42);

        assertTrue(players.getOnlinePlayers().isEmpty());
        module.spawn(player);
        var snapshot = players.getOnlinePlayers();
        assertEquals(List.of(player), snapshot);
        assertThrows(UnsupportedOperationException.class, snapshot::clear);

        module.despawn(player);
        assertTrue(players.getOnlinePlayers().isEmpty());
        assertEquals(List.of(player), snapshot);
        verify(storage).unloadMobile(player);
    }
}
