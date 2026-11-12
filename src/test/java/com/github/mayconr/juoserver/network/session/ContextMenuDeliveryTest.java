package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ContextMenuRequested;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.i18n.MessageLocalizer;
import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.session.handler.PlayerInterfaceHandler;
import io.netty.buffer.Unpooled;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContextMenuDeliveryTest {
    @Test
    void sendsFilteredEntriesOnlyToTheRequestingPlayer() {
        var session = mock(SessionView.class);
        var writer = mock(SessionPacketWriter.class);
        var player = mock(UOPlayer.class);
        var handler = new PlayerInterfaceHandler(session, writer, mock(WorldInternal.class),
                mock(MessageLocalizer.class));
        var event = new ContextMenuRequested(player, 0x40000032,
                List.of(new ContextMenuRequested.Entry(7, 3060001)));

        handler.onContextMenuRequested(event);
        verifyNoInteractions(writer);
        when(session.isPlayer(player)).thenReturn(true);

        handler.onContextMenuRequested(event);

        var packet = ArgumentCaptor.forClass(GeneralInformation.class);
        verify(writer).writeAndFlush(packet.capture());
        var buffer = Unpooled.buffer();
        try {
            packet.getValue().writesTo(buffer);
            var bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            assertEquals("bf00120014000140000032010007ea610000", HexFormat.of().formatHex(bytes));
        } finally {
            buffer.release();
        }
    }
}
