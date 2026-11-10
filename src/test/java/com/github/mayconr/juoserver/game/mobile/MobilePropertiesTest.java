package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UONpc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MobilePropertiesTest {
    private final WorldMobile mobiles = new MobileModuleImpl(null, null, null, null, new GamePlaySettings());

    @Test
    void persistentAndRuntimePropertiesAreIndependent() {
        var mobile = mobile();
        mobiles.setProperty(mobile, "craft.level", 10, false);
        mobiles.setProperty(mobile, "craft.level", 20, true);

        assertEquals(10, mobiles.getProperty(mobile, "craft.level", false));
        assertEquals(20, mobiles.getProperty(mobile, "craft.level", true));
        assertEquals(10, mobile.toData().getPersistentAttrMap().get("craft.level"));
        var reloaded = new UONpc(mobile.toData());
        assertEquals(10, mobiles.getProperty(reloaded, "craft.level", false));
        assertNull(mobiles.getProperty(reloaded, "craft.level", true));
    }

    @Test
    void missingPropertiesUseDefaultsWithoutStoringThem() {
        var mobile = mobile();

        assertNull(mobiles.getProperty(mobile, "missing", false));
        assertNull(mobiles.getProperty(mobile, "missing", true));
        assertEquals("fallback", mobiles.getProperty(mobile, "missing", "fallback", false));
        assertEquals("fallback", mobiles.getProperty(mobile, "missing", "fallback", true));
        assertFalse(mobile.persistentAttributes().contains("missing"));
        assertFalse(mobile.runtimeAttributes().contains("missing"));
    }

    @Test
    void valuesCanBeReplacedAndRemoved() {
        var mobile = mobile();
        mobiles.setProperty(mobile, "key", "first", false);
        mobiles.setProperty(mobile, "key", "first", true);
        mobiles.setProperty(mobile, "key", "second", false);
        mobiles.setProperty(mobile, "key", "second", true);

        assertEquals("second", mobiles.getProperty(mobile, "key", "fallback", false));
        assertEquals("second", mobiles.getProperty(mobile, "key", "fallback", true));
        mobiles.setProperty(mobile, "key", null, false);
        mobiles.setProperty(mobile, "key", null, true);
        assertNull(mobiles.getProperty(mobile, "key", false));
        assertNull(mobiles.getProperty(mobile, "key", true));
    }

    @Test
    void everyReadAndWriteHoldsTheSameMobileMonitor() {
        var mobile = mock(UOMobile.class);
        var persistent = new DefaultAttributeMap();
        var runtime = new DefaultAttributeMap();
        when(mobile.persistentAttributes()).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(mobile));
            return persistent;
        });
        when(mobile.runtimeAttributes()).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(mobile));
            return runtime;
        });

        mobiles.setProperty(mobile, "key", 1, false);
        mobiles.setProperty(mobile, "key", 2, true);
        assertEquals(1, mobiles.getProperty(mobile, "key", false));
        assertEquals(2, mobiles.getProperty(mobile, "key", true));
        assertEquals("default", mobiles.getProperty(mobile, "missing", "default", false));
        assertEquals("default", mobiles.getProperty(mobile, "missing", "default", true));
        mobiles.<Integer>computeProperty(mobile, "key", (key, value) -> {
            assertTrue(Thread.holdsLock(mobile));
            return value + 1;
        }, false);
        mobiles.<Integer>computeProperty(mobile, "key", (key, value) -> {
            assertTrue(Thread.holdsLock(mobile));
            return value + 1;
        }, true);
        assertEquals(2, mobiles.getProperty(mobile, "key", false));
        assertEquals(3, mobiles.getProperty(mobile, "key", true));
        assertFalse(Thread.holdsLock(mobile));
    }

    @Test
    void computationsSupportMissingValuesRemovalAndFailureWithoutReplacement() {
        var mobile = mobile();
        mobiles.<Integer>computeProperty(mobile, "count", (key, value) -> value == null ? 1 : value + 1, false);
        mobiles.<Integer>computeProperty(mobile, "count", (key, value) -> value == null ? 2 : value + 1, true);
        assertThrows(IllegalStateException.class, () -> mobiles.computeProperty(mobile, "count", (key, value) -> {
            throw new IllegalStateException("Failed update");
        }, false));
        assertEquals(1, mobiles.getProperty(mobile, "count", false));
        assertEquals(2, mobiles.getProperty(mobile, "count", true));
        mobiles.computeProperty(mobile, "count", (key, value) -> null, false);
        mobiles.computeProperty(mobile, "count", (key, value) -> null, true);
        assertNull(mobiles.getProperty(mobile, "count", false));
        assertNull(mobiles.getProperty(mobile, "count", true));
    }

    @Test
    void rejectsNullMobilesAndKeys() {
        var mobile = mobile();
        assertThrows(NullPointerException.class, () -> mobiles.setProperty(null, "key", 1, false));
        assertThrows(NullPointerException.class, () -> mobiles.setProperty(null, "key", 1, true));
        assertThrows(NullPointerException.class, () -> mobiles.getProperty(null, "key", false));
        assertThrows(NullPointerException.class, () -> mobiles.getProperty(null, "key", true));
        assertThrows(NullPointerException.class, () -> mobiles.setProperty(mobile, null, 1, false));
        assertThrows(NullPointerException.class, () -> mobiles.setProperty(mobile, null, 1, true));
        assertThrows(NullPointerException.class, () -> mobiles.getProperty(mobile, null, false));
        assertThrows(NullPointerException.class, () -> mobiles.getProperty(mobile, null, true));
    }

    private UOMobile mobile() {
        var data = new UOMobileData();
        data.setPersistentAttrMap(new DefaultAttributeMap());
        return new UONpc(data);
    }
}
