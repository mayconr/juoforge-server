package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.mobile.flow.death.transfer.MoveItemsToCorpseStep;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoveItemsToCorpseStepTest {
    @Test
    void removesMountLayerAndContinuesTransferringEquipment() {
        var data = new UOMobileData();
        var victim = new UOMobile(data);
        var mount = mock(UOItem.class);
        when(mount.getLayer()).thenReturn(Layer.MOUNT);
        when(mount.getFlags()).thenReturn(List.of(ItemFlag.MOUNT));
        var weapon = mock(UOItem.class);
        when(weapon.getLayer()).thenReturn(Layer.ONE_HANDED);
        when(weapon.getFlags()).thenReturn(List.of());
        victim.getEquippedItems().put(Layer.MOUNT, 100);
        victim.getEquippedItems().put(Layer.ONE_HANDED, 101);
        var storage = mock(RealmStorage.class);
        when(storage.getItem(100)).thenReturn(Optional.of(mount));
        when(storage.getItem(101)).thenReturn(Optional.of(weapon));
        var mobiles = mock(MobileModule.class);
        when(mobiles.unequipItem(victim, weapon)).thenAnswer(call -> {
            victim.unequipItem(weapon);
            return true;
        });
        var items = mock(ItemModule.class);
        doAnswer(call -> {
            assertFalse(victim.isMounted());
            return null;
        }).when(items).deleteItem(mount);
        var corpse = mock(UOCorpse.class);
        var context = new DeathContext(victim, null, null);
        context.corpse(corpse);

        var result = new MoveItemsToCorpseStep(mobiles, items, storage).execute(context);

        assertTrue(result.flowSucceeded());
        assertFalse(victim.isMounted());
        assertSame(mount, context.getMountItem());
        assertTrue(victim.getEquippedItems().isEmpty());
        verify(items).deleteItem(mount);
        verify(corpse).addEquippedItem(weapon);
        verify(corpse, never()).addEquippedItem(mount);
    }
}
