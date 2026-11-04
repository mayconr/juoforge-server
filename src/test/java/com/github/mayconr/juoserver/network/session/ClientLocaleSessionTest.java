package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.network.session.i18n.ClientLocale;
import org.junit.jupiter.api.Test;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientLocaleSessionTest {
    @Test
    void existingViewSeesLocaleChangesAndNullDoesNotReplaceCurrentLocale() {
        var session = new NettyPlayerSession(mock(SessionPacketWriter.class), null, null, null);
        var view = new SessionView(session::getPlayer, session::getLocale, null);
        assertEquals(ClientLocale.defaultLocale(), view.locale());
        session.setLocale(ClientLocale.PT_BR);
        assertEquals(ClientLocale.PT_BR, view.locale());
        session.setLocale(ClientLocale.ES_ES);
        assertEquals(ClientLocale.ES_ES, view.locale());
        assertThrows(NullPointerException.class, () -> session.setLocale(null));
        assertEquals(ClientLocale.ES_ES, view.locale());
    }

    @Test
    void normalizesCodesIndependentlyOfJvmLocale() {
        var original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(ClientLocale.PT_BR, ClientLocale.fromCode(" PTB "));
            assertEquals(ClientLocale.EN_US, ClientLocale.fromCode("IVL"));
            assertEquals(ClientLocale.defaultLocale(), ClientLocale.fromCode(null));
            assertEquals(ClientLocale.defaultLocale(), ClientLocale.fromCode(" "));
        } finally {
            Locale.setDefault(original);
        }
    }
}
