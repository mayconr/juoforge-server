package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.SpellbookType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.SpellBookOpened;
import com.github.mayconr.juoserver.network.packet.DrawContainer;
import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SpellBookSessionTest {
    @Test
    void sendsContentsBeforeOpeningOnlyToRecipient() {
        var channel = new EmbeddedChannel();
        try {
            var session = new NettyPlayerSession(channel, null, null, null, null);
            var player = mock(UOPlayer.class);
            var event = new SpellBookOpened(player, 0x40000001, 0x0EFA, SpellbookType.MAGERY, 8L);
            session.onSpellBookOpened(event);
            channel.runPendingTasks();
            assertNull(channel.readOutbound());
            ReflectionTestUtils.setField(session, "player", mock(UOPlayer.class));
            session.onSpellBookOpened(event);
            channel.runPendingTasks();
            assertNull(channel.readOutbound());
            ReflectionTestUtils.setField(session, "player", player);
            session.onSpellBookOpened(event);
            channel.runPendingTasks();
            var contents = assertInstanceOf(GeneralInformation.class, channel.readOutbound());
            var open = assertInstanceOf(DrawContainer.class, channel.readOutbound());
            var buffer = Unpooled.buffer();
            try {
                contents.writesTo(buffer);
                assertEquals(event.bookSerialId(), buffer.getInt(7));
                assertEquals(event.modelId(), buffer.getUnsignedShort(11));
                assertEquals(event.spellMask(), buffer.getLongLE(15));
                buffer.clear();
                open.writesTo(buffer);
                assertEquals(event.bookSerialId(), buffer.getInt(1));
                assertEquals(0xFFFF, buffer.getUnsignedShort(5));
            } finally {
                buffer.release();
            }
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
