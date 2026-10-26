package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.player.PlayerModule;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorldEntrySessionTest {
    @Test
    void excludesOfflinePlayersButKeepsOnlinePlayersAndNpcs() {
        var player = mock(UOPlayer.class);
        when(player.isAlive()).thenReturn(true);
        assertFalse(MobileVisibility.isVisible(player));
        when(player.isConnected()).thenReturn(true);
        assertTrue(MobileVisibility.isVisible(player));
        when(player.isAlive()).thenReturn(false);
        assertFalse(MobileVisibility.isVisible(player));
        assertTrue(MobileVisibility.isPresent(player));
        var npc = mock(UONpc.class);
        when(npc.isAlive()).thenReturn(true);
        assertTrue(MobileVisibility.isVisible(npc));
    }

    @Test
    void cleansUpLoadCompletedAfterDisconnectWithoutActivating() {
        var channel = new EmbeddedChannel();
        try {
            var world = mock(WorldInternal.class);
            var module = mock(PlayerModule.class);
            var bus = mock(EventBus.class);
            when(world.player()).thenReturn(module);
            var loading = new CompletableFuture<UOMobile>();
            when(module.loadMobile(1)).thenReturn(loading);
            var session = new NettyPlayerSession(channel, null, null, bus, world);
            ReflectionTestUtils.setField(session, "state", SessionState.CHARACTER_SELECTED);
            ReflectionTestUtils.setField(session, "mobileSerialId", 1);
            var entry = session.enteringWorld();
            session.disconnect();
            var player = mock(UOPlayer.class);
            loading.complete(player);
            assertTrue(entry.isCompletedExceptionally());
            assertNull(session.getPlayer());
            verify(module).despawn(player);
            assertThrows(java.util.concurrent.CancellationException.class, session::activate);
            verify(player, never()).setConnected(true);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void activatesOnlyOnce() {
        var channel = new EmbeddedChannel();
        try {
            var world = mock(WorldInternal.class);
            var module = mock(PlayerModule.class);
            var bus = mock(EventBus.class);
            when(world.player()).thenReturn(module);
            var player = mock(UOPlayer.class);
            when(module.loadMobile(1)).thenReturn(CompletableFuture.completedFuture(player));
            var session = new NettyPlayerSession(channel, null, null, bus, world);
            ReflectionTestUtils.setField(session, "state", SessionState.CHARACTER_SELECTED);
            ReflectionTestUtils.setField(session, "mobileSerialId", 1);
            assertSame(player, session.enteringWorld().join());
            session.activate();
            assertThrows(IllegalStateException.class, session::activate);
            verify(player, times(1)).setConnected(true);
            session.disconnect();
            assertThrows(java.util.concurrent.CancellationException.class, session::activate);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void invalidMobileFailsTheFutureAndSkipsActivation() {
        var channel = new EmbeddedChannel();
        try {
            var world = mock(WorldInternal.class);
            var module = mock(PlayerModule.class);
            var bus = mock(EventBus.class);
            when(world.player()).thenReturn(module);
            when(module.loadMobile(1)).thenReturn(CompletableFuture.completedFuture(mock(UONpc.class)));
            var session = new NettyPlayerSession(channel, null, null, bus, world);
            ReflectionTestUtils.setField(session, "state", SessionState.CHARACTER_SELECTED);
            ReflectionTestUtils.setField(session, "mobileSerialId", 1);
            var activation = mock(Runnable.class);
            var result = session.enteringWorld().thenRun(activation);
            assertTrue(result.isCompletedExceptionally());
            verifyNoInteractions(activation);
            assertNull(session.getPlayer());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
