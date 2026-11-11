package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.infrastructure.i18n.MessageLocalizer;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.GumpExpired;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.support.TestFields;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GumpExpirationSessionTest {
    @ParameterizedTest
    @ValueSource(ints = {12345, -1, Integer.MIN_VALUE})
    void sendsSilentCloseOnlyToTheActiveOwner(int gumpId) {
        var bus = new DefaultEventBus();
        var manager = new NettySessionManager(mock(WorldInternal.class, RETURNS_DEEP_STUBS),
                null, new GamePlaySettings(), bus, mock(MessageLocalizer.class), null);
        var ownerChannel = new EmbeddedChannel();
        var otherChannel = new EmbeddedChannel();
        try {
            var owner = manager.createSession(ownerChannel);
            var other = manager.createSession(otherChannel);
            var player = mock(UOPlayer.class);
            when(player.isConnected()).thenReturn(true);
            var otherPlayer = mock(UOPlayer.class);
            when(otherPlayer.isConnected()).thenReturn(true);
            TestFields.setField(owner, "player", player);
            TestFields.setField(other, "player", otherPlayer);
            TestFields.setField(other, "state", SessionState.ACTIVE);

            var event = new GumpExpired(player, gumpId);
            bus.publish(event);
            ownerChannel.runPendingTasks();
            otherChannel.runPendingTasks();
            assertNull(ownerChannel.readOutbound());
            assertNull(otherChannel.readOutbound());

            TestFields.setField(owner, "state", SessionState.ACTIVE);
            bus.publish(event);
            ownerChannel.runPendingTasks();
            otherChannel.runPendingTasks();
            var packet = assertInstanceOf(GeneralInformation.class, ownerChannel.readOutbound());
            var buffer = Unpooled.buffer();
            try {
                packet.writesTo(buffer);
                assertEquals(13, buffer.readableBytes());
                assertEquals(0xBF, buffer.readUnsignedByte());
                assertEquals(13, buffer.readUnsignedShort());
                assertEquals(0x0004, buffer.readUnsignedShort());
                assertEquals(gumpId, buffer.readInt());
                assertEquals(0, buffer.readInt());
                assertFalse(buffer.isReadable());
            } finally {
                buffer.release();
            }
            assertNull(ownerChannel.readOutbound());
            assertNull(otherChannel.readOutbound());
        } finally {
            ownerChannel.finishAndReleaseAll();
            otherChannel.finishAndReleaseAll();
        }
    }
}
