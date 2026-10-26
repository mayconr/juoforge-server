package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.network.packet.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NettySessionManagerTest {
    @Test
    void routesAllHandlerGroupsAndUnregistersThemWhenSessionCloses() {
        var bus = new DefaultEventBus();
        var world = mock(WorldInternal.class, RETURNS_DEEP_STUBS);
        var manager = new NettySessionManager(world, null, mock(com.github.mayconr.juoserver.game.GamePlaySettings.class, RETURNS_DEEP_STUBS), bus);
        var channel = new EmbeddedChannel();
        try {
            var session = manager.createSession(channel);
            var player = mock(UOPlayer.class);
            when(player.getId()).thenReturn(UUID.randomUUID());
            when(player.isConnected()).thenReturn(true);
            ReflectionTestUtils.setField(session, "player", player);
            ReflectionTestUtils.setField(session, "state", SessionState.ACTIVE);
            var npc = mock(UONpc.class);
            when(world.mobile().getEquippedItems(npc)).thenReturn(Map.of());
            var raw = mock(Packet.class);
            bus.publish(new NpcCreated(npc));
            bus.publish(new ItemDeleted(mock(UOItem.class)));
            bus.publish(new RawPacketSent(player, raw));
            bus.publish(new MobileDamagedEvent(player, player, null, List.of(), 5, 10, 5));
            channel.runPendingTasks();
            assertInstanceOf(DrawMobile.class, channel.readOutbound());
            assertInstanceOf(DeleteObject.class, channel.readOutbound());
            assertSame(raw, channel.readOutbound());
            assertInstanceOf(Damage.class, channel.readOutbound());
            assertInstanceOf(StatusBarInfo.class, channel.readOutbound());
            assertNull(channel.readOutbound());
            var listeners = (Map<?, ?>) ReflectionTestUtils.getField(bus, "listeners");
            assertNotNull(listeners);
            assertFalse(listeners.isEmpty());
            channel.close();
            channel.runPendingTasks();
            assertTrue(listeners.isEmpty());
            bus.publish(new RawPacketSent(player, raw));
            channel.runPendingTasks();
            assertNull(channel.readOutbound());
            verify(player).setConnected(false);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void sendsRemovalOncePerActiveClientAndIgnoresUnassignedSessions() {
        var bus = new DefaultEventBus();
        var group = mock(io.netty.channel.group.ChannelGroup.class);
        var manager = new NettySessionManager(null, group, null, bus);
        var first = new EmbeddedChannel();
        var second = new EmbeddedChannel();
        var selecting = new EmbeddedChannel();
        try {
            var firstSession = manager.createSession(first);
            var secondSession = manager.createSession(second);
            manager.createSession(selecting);
            for (var session : List.of(firstSession, secondSession)) {
                ReflectionTestUtils.setField(session, "player", mock(UOPlayer.class));
                ReflectionTestUtils.setField(session, "state", SessionState.ACTIVE);
            }
            bus.publish(new NpcRemoved(mock(UONpc.class)));
            first.runPendingTasks();
            second.runPendingTasks();
            selecting.runPendingTasks();
            assertInstanceOf(DeleteObject.class, first.readOutbound());
            assertInstanceOf(DeleteObject.class, second.readOutbound());
            assertNull(first.readOutbound());
            assertNull(second.readOutbound());
            assertNull(selecting.readOutbound());
            verifyNoInteractions(group);
            bus.publish(new ItemDeleted(mock(UOItem.class)));
            firstSession.disconnect();
            first.runPendingTasks();
            assertNull(first.readOutbound());
        } finally {
            first.finishAndReleaseAll();
            second.finishAndReleaseAll();
            selecting.finishAndReleaseAll();
        }
    }

}
