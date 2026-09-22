package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import java.util.Optional;
import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SpellSelectionHandlerTest {
    @Test
    void resolvesPacketSpellIdToInternalKey() {
        var world = mock(WorldInternal.class);
        var session = mock(PlayerSession.class);
        var player = mock(UOPlayer.class);
        when(session.getPlayer()).thenReturn(player);
        when(world.getSpellByClientId(0x0123)).thenReturn(Optional.of(
                new SpellTemplate("shard:mapped", 0x0123, "Mapped", null)));
        var buffer = Unpooled.wrappedBuffer(new byte[] {
                (byte) 0xBF, 0, 9, 0, 0x1C, 0, 2, 0x01, 0x23
        });
        try {
            new GeneralInformationHandler(world).channelRead0(session, null, new GeneralInformation(buffer));
            verify(world).getSpellByClientId(0x0123);
            verify(world).castSpell(player, "shard:mapped");
            verifyNoMoreInteractions(world);
        } finally {
            buffer.release();
        }
    }

    @Test
    void unknownClientIdDoesNotCastSpell() {
        var world = mock(WorldInternal.class);
        var session = mock(PlayerSession.class);
        when(session.getPlayer()).thenReturn(mock(UOPlayer.class));
        when(world.getSpellByClientId(65535)).thenReturn(Optional.empty());
        var buffer = Unpooled.wrappedBuffer(new byte[] {
                (byte) 0xBF, 0, 9, 0, 0x1C, 0, 2, (byte) 0xFF, (byte) 0xFF
        });
        try {
            new GeneralInformationHandler(world).channelRead0(session, null, new GeneralInformation(buffer));
            verify(world).getSpellByClientId(65535);
            verifyNoMoreInteractions(world);
        } finally {
            buffer.release();
        }
    }

    @Test
    void ignoresSpellSelectionWithoutActivePlayer() {
        var world = mock(WorldInternal.class);
        var session = mock(PlayerSession.class);
        var buffer = Unpooled.wrappedBuffer(new byte[] {
                (byte) 0xBF, 0, 9, 0, 0x1C, 0, 2, 0, 1
        });
        try {
            new GeneralInformationHandler(world).channelRead0(session, null, new GeneralInformation(buffer));
            verifyNoInteractions(world);
        } finally {
            buffer.release();
        }
    }
}
