package com.github.mayconr.juoserver.game.ui.contextmenu;

import com.github.mayconr.juoserver.infrastructure.i18n.ClientLocale;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalizedClilocTest {
    @Test
    void resolvesTheClilocOfTheLocale() {
        assertEquals(3060002, localized().resolve(ClientLocale.EN_US));
    }

    @Test
    void fallsBackToTheFirstClilocWhenTheLocaleHasNoMatch() {
        assertEquals(3060001, localized().resolve(ClientLocale.ES_ES));
        assertEquals(3060001, localized().resolve(null));
    }

    @Test
    void usesASingleClilocForEveryLocale() {
        var cliloc = LocalizedCliloc.of(3060001);

        assertEquals(3060001, cliloc.resolve(ClientLocale.PT_BR));
        assertEquals(3060001, cliloc.resolve(ClientLocale.EN_US));
    }

    @Test
    void rejectsEmptyAndOutOfRangeClilocs() {
        var outOfRange = new LinkedHashMap<ClientLocale, Integer>();
        outOfRange.put(ClientLocale.EN_US, 1000);

        assertThrows(IllegalArgumentException.class, () -> new LocalizedCliloc(new LinkedHashMap<>()));
        assertThrows(IllegalArgumentException.class, () -> new LocalizedCliloc(outOfRange));
    }

    @Test
    void keepsItsClilocsWhenTheSourceMapChanges() {
        var clilocs = new LinkedHashMap<ClientLocale, Integer>();
        clilocs.put(ClientLocale.PT_BR, 3060001);
        var cliloc = new LocalizedCliloc(clilocs);

        clilocs.put(ClientLocale.EN_US, 3060002);

        assertEquals(3060001, cliloc.resolve(ClientLocale.EN_US));
    }

    private static LocalizedCliloc localized() {
        var clilocs = new LinkedHashMap<ClientLocale, Integer>();
        clilocs.put(ClientLocale.PT_BR, 3060001);
        clilocs.put(ClientLocale.EN_US, 3060002);
        return new LocalizedCliloc(clilocs);
    }
}
