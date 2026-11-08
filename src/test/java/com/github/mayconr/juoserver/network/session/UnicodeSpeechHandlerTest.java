package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.model.event.message.*;
import com.github.mayconr.juoserver.network.packet.SendUnicodeSpeech;
import com.github.mayconr.juoserver.network.session.handler.PlayerInterfaceHandler;
import com.github.mayconr.juoserver.infrastructure.i18n.*;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnicodeSpeechHandlerTest {
    @Test
    void sendsLocalizedAndPlainMessagesAsUnicode() {
        var view = mock(SessionView.class);
        var writer = mock(SessionPacketWriter.class);
        var localizer = mock(MessageLocalizer.class);
        when(view.locale()).thenReturn(ClientLocale.PT_BR);
        var player = mock(UOPlayer.class);
        when(view.isPlayer(player)).thenReturn(true);
        var handler = new PlayerInterfaceHandler(view, writer, null, localizer);
        var content = MessageContent.localized("mining.found");
        when(localizer.localize(content, ClientLocale.PT_BR)).thenReturn("Você encontrou minério.");
        var style = new MessageStyleTemplate("system", TextType.BROADCAST, 0x03B2, 3);
        handler.onMessageSent(new MessageSent(content, null, player, style));
        handler.onMessageSent(new MessageSent(MessageContent.plain("Ação e coração."), null, player, style));
        var packets = ArgumentCaptor.forClass(SendUnicodeSpeech.class);
        verify(writer, times(2)).writeAndFlush(packets.capture());
        assertText(packets.getAllValues().get(0), "Você encontrou minério.", "PTB");
        assertText(packets.getAllValues().get(1), "Ação e coração.", "PTB");
        verify(localizer).localize(content, ClientLocale.PT_BR);
    }

    @Test
    void sendsMobileSpeechAsUnicodeWithDefaultLanguageWhenLocaleIsMissing() {
        var view = mock(SessionView.class);
        var writer = mock(SessionPacketWriter.class);
        var mobile = mock(UOMobile.class);
        when(mobile.getDisplayName()).thenReturn("Miner");
        when(view.shouldReceiveUpdate(mobile)).thenReturn(true);
        var handler = new PlayerInterfaceHandler(view, writer, null, mock(MessageLocalizer.class));
        handler.onMobileSpeech(new MobileSpeech(mobile, "Olá!",
                new SpeechContext(TextType.BROADCAST, SpeechRange.NORMAL, 0x03B2, 3, 0, mobile)));
        var packet = ArgumentCaptor.forClass(SendUnicodeSpeech.class);
        verify(writer).writeAndFlush(packet.capture());
        assertText(packet.getValue(), "Olá!", "ENU");
    }

    private void assertText(SendUnicodeSpeech packet, String text, String language) {
        var buf = Unpooled.buffer();
        try {
            packet.writesTo(buf);
            assertEquals(language, buf.toString(14, 3, StandardCharsets.US_ASCII));
            assertEquals(text, buf.toString(48, buf.readableBytes() - 50, StandardCharsets.UTF_16BE));
        } finally {
            buf.release();
        }
    }
}
