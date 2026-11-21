package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.mobile.flow.death.transfer.MoveItemsToCorpseStep;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemResistances;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
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
        when(mount.getResistances()).thenReturn(ItemResistances.NONE);
        when(mount.getLayer()).thenReturn(Layer.MOUNT);
        when(mount.getSerialId()).thenReturn(100);
        when(mount.getCurrentLocation()).thenReturn(ItemLocation.equipped(victim.getSerialId()));
        when(mount.getFlags()).thenReturn(List.of(ItemFlag.MOUNT));
        var weapon = mock(UOItem.class);
        when(weapon.getResistances()).thenReturn(ItemResistances.NONE);
        when(weapon.getLayer()).thenReturn(Layer.ONE_HANDED);
        when(weapon.getSerialId()).thenReturn(101);
        when(weapon.getCurrentLocation()).thenReturn(ItemLocation.equipped(victim.getSerialId()));
        when(weapon.getFlags()).thenReturn(List.of());
        victim.getEquippedItems().put(Layer.MOUNT, 100);
        victim.getEquippedItems().put(Layer.ONE_HANDED, 101);
        var storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
        when(storage.items().get(100)).thenReturn(Optional.of(mount));
        when(storage.items().get(101)).thenReturn(Optional.of(weapon));
        when(storage.mobiles().get(victim.getSerialId())).thenReturn(Optional.of(victim));
        var mobiles = mock(MobileModule.class);
        var items = mock(ItemModule.class);
        doAnswer(call -> {
            assertFalse(victim.isMounted());
            return null;
        }).when(items).deleteItem(mount);
        var corpse = mock(UOCorpse.class);
        when(corpse.getCurrentLocation()).thenReturn(ItemLocation.ground());
        var context = new DeathContext(victim, null, null);
        context.corpse(corpse);

        var result = new MoveItemsToCorpseStep(mobiles, items, storage).execute(context);

        assertTrue(result.flowSucceeded());
        assertFalse(victim.isMounted());
        assertSame(mount, context.getMountItem());
        assertTrue(victim.getEquippedItems().isEmpty());
        verify(items).deleteItem(mount);
        verify(mobiles, never()).unequipItem(any(UOMobile.class), any(UOItem.class));
        verify(corpse).addItemToContainer(weapon);
        verify(corpse).recordEquippedItem(weapon);
        verify(corpse, never()).recordEquippedItem(mount);
    }
}
