package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.RawPacketSent;
import com.github.mayconr.juoserver.game.model.event.RawPacketsSent;
import com.github.mayconr.juoserver.network.packet.Packet;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RawPacketsSessionTest {
    @Test
    void preservesBatchOrderAndCopiesListBeforeAsynchronousDelivery() {
        var channel = new EmbeddedChannel();
        try {
            var session = new NettyPlayerSession(channel, null, null, null, null);
            var player = mock(UOPlayer.class);
            when(player.getId()).thenReturn(UUID.randomUUID());
            ReflectionTestUtils.setField(session, "player", player);
            var first = mock(Packet.class);
            var second = mock(Packet.class);
            var packets = new ArrayList<>(List.of(first, second));
            var event = new RawPacketsSent(player, packets);
            session.onRawPacketsSent(event);
            packets.clear();
            channel.runPendingTasks();
            assertSame(first, channel.readOutbound());
            assertSame(second, channel.readOutbound());
            assertNull(channel.readOutbound());
            assertThrows(UnsupportedOperationException.class, () -> event.packets().clear());
            session.onRawPacketSent(new RawPacketSent(player, first));
            channel.runPendingTasks();
            assertSame(first, channel.readOutbound());
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void ignoresUnassignedSessionsOtherRecipientsEmptyBatchesAndClosedChannels() {
        var channel = new EmbeddedChannel();
        try {
            var session = new NettyPlayerSession(channel, null, null, null, null);
            var player = mock(UOPlayer.class);
            when(player.getId()).thenReturn(UUID.randomUUID());
            var other = mock(UOPlayer.class);
            when(other.getId()).thenReturn(UUID.randomUUID());
            var packet = mock(Packet.class);
            var event = new RawPacketSent(player, packet);
            session.onRawPacketSent(event);
            ReflectionTestUtils.setField(session, "player", other);
            session.onRawPacketSent(event);
            ReflectionTestUtils.setField(session, "player", player);
            session.onRawPacketsSent(new RawPacketsSent(player, List.of()));
            channel.runPendingTasks();
            assertNull(channel.readOutbound());
            channel.close();
            session.onRawPacketSent(event);
            channel.runPendingTasks();
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
