package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ContextMenuRequested;
import com.github.mayconr.juoserver.game.ui.contextmenu.LocalizedCliloc;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.i18n.ClientLocale;
import com.github.mayconr.juoserver.infrastructure.i18n.MessageLocalizer;
import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.session.handler.PlayerInterfaceHandler;
import io.netty.buffer.Unpooled;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContextMenuDeliveryTest {
    private final SessionView session = mock(SessionView.class);
    private final SessionPacketWriter writer = mock(SessionPacketWriter.class);
    private final UOPlayer player = mock(UOPlayer.class);
    private final PlayerInterfaceHandler handler = new PlayerInterfaceHandler(session, writer,
            mock(WorldInternal.class), mock(MessageLocalizer.class));

    @Test
    void sendsFilteredEntriesOnlyToTheRequestingPlayer() {
        var event = new ContextMenuRequested(player, 0x40000032,
                List.of(new ContextMenuRequested.Entry(7, LocalizedCliloc.of(3060001))));

        handler.onContextMenuRequested(event);
        verifyNoInteractions(writer);
        when(session.isPlayer(player)).thenReturn(true);

        handler.onContextMenuRequested(event);

        assertEquals("bf00120014000140000032010007ea610000", sentHex());
    }

    @Test
    void sendsTheClilocOfTheSessionLocale() {
        when(session.isPlayer(player)).thenReturn(true);
        when(session.locale()).thenReturn(ClientLocale.PT_BR);

        handler.onContextMenuRequested(localizedEvent());

        assertEquals("bf00120014000140000032010007ea620000", sentHex());
    }

    @Test
    void sendsTheFirstClilocWhenTheSessionLocaleHasNoMatch() {
        when(session.isPlayer(player)).thenReturn(true);
        when(session.locale()).thenReturn(ClientLocale.ES_ES);

        handler.onContextMenuRequested(localizedEvent());

        assertEquals("bf00120014000140000032010007ea610000", sentHex());
    }

    private ContextMenuRequested localizedEvent() {
        var clilocs = new LinkedHashMap<ClientLocale, Integer>();
        clilocs.put(ClientLocale.EN_US, 3060001);
        clilocs.put(ClientLocale.PT_BR, 3060002);
        return new ContextMenuRequested(player, 0x40000032,
                List.of(new ContextMenuRequested.Entry(7, new LocalizedCliloc(clilocs))));
    }

    private String sentHex() {
        var packet = ArgumentCaptor.forClass(GeneralInformation.class);
        verify(writer).writeAndFlush(packet.capture());
        var buffer = Unpooled.buffer();
        try {
            packet.getValue().writesTo(buffer);
            var bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return HexFormat.of().formatHex(bytes);
        } finally {
            buffer.release();
        }
    }
}
