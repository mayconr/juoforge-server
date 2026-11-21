package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemFlowDefinition;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.ItemResistances;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedOnTheGround;
import com.github.mayconr.juoserver.game.model.event.ItemUnequipped;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UnequipDestinationTest {
    private final WorldStorage storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
    private final EventBus events = mock(EventBus.class);
    private final ItemModule items = mock(ItemModule.class);
    private final MobileModule mobiles = new MobileModuleImpl(null, null, events, storage, new GamePlaySettings());
    private final GameInfra infra = GameInfra.builder().storage(storage).eventBus(events).build();

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void placesPlayerAndNpcEquipmentOnTheGround(boolean player) {
        UOMobile mobile = player ? new UOPlayer(new UOMobileData()) : new UOMobile(new UOMobileData());
        var item = equipment(mobile);
        var context = new UnequipItemContext(mobile, item, ItemTarget.dropAt(mobile));

        assertTrue(flow().execute(context).flowSucceeded());

        assertTrue(context.isUnequipped());
        assertFalse(mobile.isItemEquipped(item));
        assertInstanceOf(GroundLocation.class, item.getCurrentLocation());
        assertEquals(mobile.getX(), item.getX());
        assertEquals(0, mobile.getPhysicalResist());
        verify(storage.items()).placeOnGround(item);
        verify(items).recordGroundInteraction(item);
        verify(events).publish(new ItemDroppedOnTheGround(mobile, item, mobile));
        verify(events).publish(new ItemUnequipped(mobile, item));
    }

    @Test
    void orphanPreservesTheLiftBehaviorWithoutDropping() {
        var mobile = new UOMobile(new UOMobileData());
        var item = equipment(mobile);
        var context = new UnequipItemContext(mobile, item, ItemTarget.orphan());

        assertTrue(flow().execute(context).flowSucceeded());

        assertTrue(context.isUnequipped());
        assertInstanceOf(OrphanLocation.class, item.getCurrentLocation());
        assertEquals(0, mobile.getPhysicalResist());
        verify(storage.items(), never()).placeOnGround(any());
        verify(events, never()).publish(any(ItemDroppedOnTheGround.class));
        verify(events).publish(new ItemUnequipped(mobile, item));
    }

    @Test
    void outOfRangeDropLeavesEquipmentAndResistancesUntouched() {
        var mobile = new UOMobile(new UOMobileData());
        var item = equipment(mobile);
        var destination = mock(Location.class);
        when(destination.getX()).thenReturn(100);
        var context = new UnequipItemContext(mobile, item, ItemTarget.dropAt(destination));

        assertEquals("DROP_OUT_OF_RANGE", flow().execute(context).code());

        assertFalse(context.isUnequipped());
        assertTrue(mobile.isItemEquipped(item));
        assertInstanceOf(EquippedLocation.class, item.getCurrentLocation());
        assertEquals(5, mobile.getPhysicalResist());
        verify(storage.items(), never()).placeOnGround(any());
        verify(events, never()).publish(any(ItemUnequipped.class));
    }

    @Test
    void failedGroundPlacementPreservesEquipmentAndPosition() {
        var mobile = new UOMobile(new UOMobileData());
        var item = equipment(mobile);
        item.setX(42);
        var worldItems = storage.items();
        doThrow(new IllegalStateException("Ground index unavailable")).when(worldItems).placeOnGround(item);
        var context = new UnequipItemContext(mobile, item, ItemTarget.dropAt(mobile));

        assertTrue(flow().execute(context).flowFailed());

        assertFalse(context.isUnequipped());
        assertTrue(mobile.isItemEquipped(item));
        assertInstanceOf(EquippedLocation.class, item.getCurrentLocation());
        assertEquals(42, item.getX());
        assertEquals(5, mobile.getPhysicalResist());
        verify(events, never()).publish(any(ItemUnequipped.class));
    }

    @Test
    void unsupportedTargetDoesNotRemoveEquipment() {
        var mobile = new UOMobile(new UOMobileData());
        var item = equipment(mobile);
        var context = new UnequipItemContext(mobile, item, ItemTarget.equip(mobile));

        assertEquals("UNEQUIP_INVALID_TARGET", flow().execute(context).code());
        assertTrue(mobile.isItemEquipped(item));
        assertEquals(5, mobile.getPhysicalResist());
    }

    private Flow<UnequipItemContext> flow() {
        return UnequipItemFlowDefinition.build(infra, mobiles, items);
    }

    private UOItem equipment(UOMobile mobile) {
        var data = new UOItemData();
        data.setSerialId(0x40000001);
        data.setLocationType(ItemLocationType.ORPHAN);
        data.setLayer(Layer.ONE_HANDED);
        data.setFlags(List.of(ItemFlag.WEARABLE));
        data.setResistances(new ItemResistances(5, 0, 0, 0, 0));
        var item = new UOItem(data, null);
        mobile.equipItem(item);
        item.setCurrentLocation(ItemLocation.equipped(mobile.getSerialId()));
        mobile.setPhysicalResist(5);
        return item;
    }
}
