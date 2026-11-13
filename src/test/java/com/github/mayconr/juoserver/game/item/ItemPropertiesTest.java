package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.decay.ItemDecayState;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemPropertiesTest {
    private final ItemModuleImpl items = new ItemModuleImpl(null, null, null);

    @Test
    void persistentAndRuntimePropertiesAreIndependentAndNullRemovesThem() {
        var data = new UOItemData();
        data.setLocationType(ItemLocationType.GROUND);
        data.setPersistentAttrMap(new DefaultAttributeMap());
        var item = new UOItem(data, null);

        items.setProperty(item, "value", 10, false);
        items.setProperty(item, "value", 20, true);
        assertEquals(10, items.getProperty(item, "value", false));
        assertEquals(20, items.getProperty(item, "value", true));
        var restored = new UOItem(item.toData(), null);
        assertEquals(10, items.getProperty(restored, "value", false));
        assertNull(items.getProperty(restored, "value", true));
        items.setProperty(item, "value", null, false);
        assertNull(items.getProperty(item, "value", false));
        assertEquals(20, items.getProperty(item, "value", true));
        assertEquals("fallback", items.getProperty(item, "missing", "fallback", false));
        assertFalse(item.persistentAttributes().contains("missing"));
    }

    @Test
    void readsWritesAndAtomicUpdatesUseTheSameItemMonitor() {
        var item = mock(UOItem.class);
        var persistent = new DefaultAttributeMap();
        var runtime = new DefaultAttributeMap();
        when(item.persistentAttributes()).thenAnswer(call -> {
            assertTrue(Thread.holdsLock(item));
            return persistent;
        });
        when(item.runtimeAttributes()).thenAnswer(call -> {
            assertTrue(Thread.holdsLock(item));
            return runtime;
        });

        items.setProperty(item, "value", 1, false);
        items.setProperty(item, "value", 2, true);
        assertEquals(1, items.getProperty(item, "value", false));
        assertEquals(2, items.getProperty(item, "value", true));
        items.<Integer>computeProperty(item, "value", (key, value) -> {
            assertTrue(Thread.holdsLock(item));
            return value + 1;
        }, false);
        assertEquals(2, items.getProperty(item, "value", false));
        assertThrows(IllegalStateException.class, () ->
                items.computeProperty(item, "value", (key, value) -> {
                    throw new IllegalStateException("Cannot update");
                }, false));
        assertEquals(2, items.getProperty(item, "value", false));
        items.computeProperty(item, "value", (key, value) -> null, true);
        assertNull(items.getProperty(item, "value", true));
        assertFalse(Thread.holdsLock(item));
    }

    @Test
    void decayWritesThroughModuleAndLazyInitializationPreservesExistingDate() {
        var module = spy(items);
        var data = new UOItemData();
        data.setLocationType(ItemLocationType.GROUND);
        data.setPersistentAttrMap(new DefaultAttributeMap());
        var item = new UOItem(data, null);

        assertEquals(100L, ItemDecayState.droppedAt(module, item, 100));
        assertEquals(100L, ItemDecayState.droppedAt(module, item, 200));
        ItemDecayState.recordDrop(module, item, 300);
        verify(module).setProperty(item, ItemDecayState.DROPPED_AT, 300L, false);
        assertEquals(300L, ItemDecayState.droppedAt(module, item, 400));
    }
}
