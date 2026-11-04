package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.packet.LanguageExtendedCommand;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import com.github.mayconr.juoserver.network.session.i18n.ClientLocale;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientLanguageHandlerTest {
    @ParameterizedTest
    @CsvSource({"PTB,PT_BR", "ENU,EN_US", "ESP,ES_ES", "ptb,PT_BR", "IVL,EN_US", "XXX,EN_US"})
    void decodesLanguageAndUpdatesSession(String code, ClientLocale expected) {
        var buffer = Unpooled.buffer();
        try {
            buffer.writeByte(0xBF);
            buffer.writeShort(9);
            buffer.writeShort(0x000B);
            buffer.writeCharSequence(code, StandardCharsets.US_ASCII);
            buffer.writeByte(0);
            var packet = new GeneralInformation(buffer);
            assertEquals(code, ((LanguageExtendedCommand) packet.getCommand()).language());
            assertEquals(0, buffer.readableBytes());
            var session = mock(PlayerSession.class);
            new GeneralInformationHandler(null).channelRead0(session, null, packet);
            verify(session).setLocale(expected);
            verifyNoMoreInteractions(session);
        } finally {
            buffer.release();
        }
    }
}
