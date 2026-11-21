package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.model.ResistanceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceEquippedItemStep;
import com.github.mayconr.juoserver.game.item.hold.HeldItem;
import com.github.mayconr.juoserver.game.item.hold.RestoreHeldItem;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.shared.InitializeEquipmentResistancesStep;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.EquipItemTarget;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.ItemResistances;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileResistancesChanged;
import com.github.mayconr.juoserver.game.model.event.MobileResistancesChanged.Values;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

class EquipmentResistancesTest {
    private final EventBus events = mock(EventBus.class);
    private final WorldStorage storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
    private final WorldMobile mobiles = new MobileModuleImpl(null, null, events, storage, new GamePlaySettings());
    private final GameInfra infra = GameInfra.builder().storage(storage).eventBus(events).build();

    @Test
    void equippingAndRemovingMultiplePiecesPreservesInnateValuesAndCompleteContributions() {
        var mobile = new UOMobile(new UOMobileData());
        mobiles.setResistance(mobile, ResistanceType.PHYSICAL, 2);
        mobiles.setResistance(mobile, ResistanceType.FIRE, 3);
        mobiles.setResistance(mobile, ResistanceType.COLD, 4);
        mobiles.setResistance(mobile, ResistanceType.POISON, 5);
        mobiles.setResistance(mobile, ResistanceType.ENERGY, 6);
        var chest = item(100, Layer.INNER_TORSO, new ItemResistances(30, 10, 20, 30, 40));
        var helm = item(101, Layer.HEAD, new ItemResistances(50, 1, 2, 3, 4));
        clearInvocations(events);
        equip(mobile, chest);
        equip(mobile, helm);
        assertResistances(mobile, 82, 14, 26, 38, 50);
        verify(events, times(10)).publish(any(MobileResistancesChanged.class));
        clearInvocations(events);
        unequip(mobile, chest);
        assertResistances(mobile, 52, 4, 6, 8, 10);
        verify(events, times(5)).publish(any(MobileResistancesChanged.class));
        verify(events).publish(new MobileResistancesChanged(mobile, new Values(82, 14, 26, 38, 50)));
        unequip(mobile, helm);
        assertResistances(mobile, 2, 3, 4, 5, 6);
    }

    @Test
    void occupiedLayerAndRepeatedUnequipDoNotChangeResistances() {
        var mobile = new UOMobile(new UOMobileData());
        var chest = item(100, Layer.INNER_TORSO, new ItemResistances(5, 0, 0, 0, 0));
        equip(mobile, chest);
        var other = item(101, Layer.INNER_TORSO, new ItemResistances(10, 0, 0, 0, 0));
        assertTrue(EquipItemFlowDefinition.build(infra, mobiles).execute(new EquipItemContext(mobile, other)).flowFailed());
        assertEquals(5, mobile.getPhysicalResist());
        unequip(mobile, chest);
        clearInvocations(events);
        assertTrue(UnequipItemFlowDefinition.build(infra, mobiles, mock(ItemModule.class)).execute(new UnequipItemContext(mobile, chest)).flowFailed());
        assertEquals(0, mobile.getPhysicalResist());
        verifyNoInteractions(events);
    }

    @Test
    void equipsItemLoadedBySerialWithoutRequest() {
        var mobile = new UOMobile(new UOMobileData());
        var chest = item(100, Layer.INNER_TORSO, new ItemResistances(5, 0, 0, 0, 0));
        when(storage.items().get(100)).thenReturn(Optional.of(chest));
        var context = new EquipItemContext(mobile, 100);
        assertTrue(EquipItemFlowDefinition.build(infra, mobiles).execute(context).flowSucceeded());
        assertTrue(context.isEquipped());
        assertEquals(5, mobile.getPhysicalResist());
    }

    @Test
    void equipsItemLoadedFromRequest() {
        var mobile = new UOMobile(new UOMobileData());
        var chest = item(100, Layer.INNER_TORSO, new ItemResistances(5, 0, 0, 0, 0));
        var request = mock(EquipItemRequest.class);
        when(request.getItemSerialId()).thenReturn(100);
        when(storage.items().get(100)).thenReturn(Optional.of(chest));
        var context = new EquipItemContext(mobile, request);
        assertTrue(EquipItemFlowDefinition.build(infra, mobiles).execute(context).flowSucceeded());
        assertEquals(100, context.getItemSerialId());
        assertEquals(5, mobile.getPhysicalResist());
    }

    @Test
    void transferringEquipmentRemovesOldOwnersContribution() {
        var first = new UOMobile(new UOMobileData());
        var secondData = new UOMobileData();
        secondData.setSerialId(2);
        var second = new UOMobile(secondData);
        var chest = item(100, Layer.INNER_TORSO, new ItemResistances(5, 2, 3, 4, 1));
        equip(first, chest);
        when(storage.mobiles().get(first.getSerialId())).thenReturn(Optional.of(first));
        equip(second, chest);
        assertResistances(first, 0, 0, 0, 0, 0);
        assertResistances(second, 5, 2, 3, 4, 1);
        assertFalse(first.isItemEquipped(chest));
    }

    @Test
    void starterEquipmentIsAppliedOnceAndPersistedTotalsAreNotReappliedOnHydration() {
        var mobile = new UOMobile(new UOMobileData());
        mobiles.setResistance(mobile, ResistanceType.PHYSICAL, 10);
        mobiles.setResistance(mobile, ResistanceType.FIRE, 20);
        mobiles.setResistance(mobile, ResistanceType.COLD, 30);
        mobiles.setResistance(mobile, ResistanceType.POISON, 40);
        mobiles.setResistance(mobile, ResistanceType.ENERGY, 50);
        var chest = item(100, Layer.INNER_TORSO, new ItemResistances(5, 2, 3, 4, 1));
        mobile.equipItem(chest);
        when(storage.items().get(100)).thenReturn(Optional.of(chest));
        new InitializeEquipmentResistancesStep<UOMobile>(value -> value, storage, mobiles).execute(mobile);
        assertResistances(mobile, 15, 22, 33, 44, 51);
        var reloaded = new UOMobile(mobile.toData());
        reloaded.equipItem(new UOItem(chest.toData(), null));
        assertResistances(reloaded, 15, 22, 33, 44, 51);
    }

    @Test
    void rejectedDropRestoresEquipmentResistance() {
        var player = new UOPlayer(new UOMobileData());
        var chest = item(100, Layer.INNER_TORSO, new ItemResistances(5, 2, 3, 4, 1));
        equip(player, chest);
        var held = HeldItem.capture(chest);
        unequip(player, chest);
        when(storage.mobiles().get(player.getSerialId())).thenReturn(Optional.of(player));
        new RestoreHeldItem(storage, ignored -> { }, mobiles).restore(held, player);
        assertTrue(player.isItemEquipped(chest));
        assertResistances(player, 5, 2, 3, 4, 1);
    }

    @Test
    void creatingAnAlreadyEquippedItemAppliesItsContribution() {
        var mobile = new UOMobile(new UOMobileData());
        var chest = item(100, Layer.INNER_TORSO, new ItemResistances(5, 2, 3, 4, 1));
        var context = new ItemCreationContext(ItemRequest.byName("platemail"), new EquipItemTarget(mobile));
        context.complete(chest);
        var items = mock(ItemModule.class);
        when(items.equipItem(mobile, chest, false)).thenAnswer(invocation -> {
            var equipment = new EquipItemContext(mobile, chest);
            equipment.setNotify(false);
            return EquipItemFlowDefinition.build(infra, mobiles).execute(equipment).flowSucceeded()
                    && equipment.isEquipped();
        });
        assertTrue(new PlaceEquippedItemStep(items).execute(context).flowSucceeded());
        assertTrue(mobile.isItemEquipped(chest));
        assertResistances(mobile, 5, 2, 3, 4, 1);
        verify(events, times(5)).publish(any(MobileResistancesChanged.class));
    }

    @Test
    void unchangedValuesDoNotPublishStatusEvents() {
        var mobile = new UOMobile(new UOMobileData());
        mobiles.setResistance(mobile, ResistanceType.PHYSICAL, 0);
        mobiles.setResistance(mobile, ResistanceType.FIRE, 0);
        mobiles.setResistance(mobile, ResistanceType.COLD, 0);
        mobiles.setResistance(mobile, ResistanceType.POISON, 0);
        mobiles.setResistance(mobile, ResistanceType.ENERGY, 0);
        verify(events, never()).publish(any(MobileResistancesChanged.class));
    }

    @Test
    void setterReplacesTotalsAndClampsEachChannelBeforeCheckingForChanges() {
        var mobile = new UOMobile(new UOMobileData());
        mobiles.setResistance(mobile, ResistanceType.PHYSICAL, -10);
        mobiles.setResistance(mobile, ResistanceType.FIRE, 120);
        mobiles.setResistance(mobile, ResistanceType.COLD, 30);
        mobiles.setResistance(mobile, ResistanceType.POISON, 101);
        mobiles.setResistance(mobile, ResistanceType.ENERGY, -1);
        assertResistances(mobile, 0, 100, 30, 100, 0);
        clearInvocations(events);
        mobiles.setResistance(mobile, ResistanceType.PHYSICAL, -20);
        mobiles.setResistance(mobile, ResistanceType.FIRE, 200);
        mobiles.setResistance(mobile, ResistanceType.COLD, 30);
        mobiles.setResistance(mobile, ResistanceType.POISON, 102);
        mobiles.setResistance(mobile, ResistanceType.ENERGY, -2);
        verify(events, never()).publish(any(MobileResistancesChanged.class));
        mobiles.setResistance(mobile, ResistanceType.PHYSICAL, 1);
        mobiles.setResistance(mobile, ResistanceType.FIRE, 2);
        mobiles.setResistance(mobile, ResistanceType.COLD, 3);
        mobiles.setResistance(mobile, ResistanceType.POISON, 4);
        mobiles.setResistance(mobile, ResistanceType.ENERGY, 5);
        assertResistances(mobile, 1, 2, 3, 4, 5);
        verify(events, times(5)).publish(any(MobileResistancesChanged.class));
    }

    @Test
    void settingOneTypePreservesEveryOtherResistance() {
        for (var type : ResistanceType.values()) {
            clearInvocations(events);
            var data = new UOMobileData();
            data.setPhysicalResist(7);
            data.setFireResist(7);
            data.setColdResist(7);
            data.setPoisonResist(7);
            data.setEnergyResist(7);
            var mobile = new UOMobile(data);
            mobiles.setResistance(mobile, type, 42);
            assertResistances(mobile,
                    type == ResistanceType.PHYSICAL ? 42 : 7,
                    type == ResistanceType.FIRE ? 42 : 7,
                    type == ResistanceType.COLD ? 42 : 7,
                    type == ResistanceType.POISON ? 42 : 7,
                    type == ResistanceType.ENERGY ? 42 : 7);
            verify(events).publish(new MobileResistancesChanged(mobile, new Values(7, 7, 7, 7, 7)));
        }
    }

    @Test
    void configuredLimitsApplyToAllResistanceChannels() {
        var settings = new GamePlaySettings().withMobile(
                new GamePlaySettings.Mobile("backpack", 2, null, null, 10, 70));
        WorldMobile configured = new MobileModuleImpl(null, null, events, storage, settings);
        var mobile = new UOMobile(new UOMobileData());
        configured.setResistance(mobile, ResistanceType.PHYSICAL, 0);
        configured.setResistance(mobile, ResistanceType.FIRE, 100);
        configured.setResistance(mobile, ResistanceType.COLD, -5);
        configured.setResistance(mobile, ResistanceType.POISON, 75);
        configured.setResistance(mobile, ResistanceType.ENERGY, 45);
        assertResistances(mobile, 10, 70, 10, 70, 45);
    }

    @Test
    void resistanceLimitsDefaultWhenOmittedAndSurviveVitalsChanges() throws Exception {
        var mapper = new ObjectMapper();
        var defaults = mapper.readValue("{}", GamePlaySettings.Mobile.class);
        assertEquals(0, defaults.minResistance());
        assertEquals(100, defaults.maxResistance());
        var custom = mapper.readValue("{\"minResistance\":10,\"maxResistance\":70}", GamePlaySettings.Mobile.class)
                .withVitals(new GamePlaySettings.Vitals(100));
        assertEquals(10, custom.minResistance());
        assertEquals(70, custom.maxResistance());
        assertThrows(IllegalArgumentException.class,
                () -> new GamePlaySettings.Mobile("backpack", 2, null, null, 80, 70));
    }

    private void equip(UOMobile mobile, UOItem item) {
        assertTrue(EquipItemFlowDefinition.build(infra, mobiles).execute(new EquipItemContext(mobile, item)).flowSucceeded());
    }

    private void unequip(UOMobile mobile, UOItem item) {
        assertTrue(UnequipItemFlowDefinition.build(infra, mobiles, mock(ItemModule.class)).execute(new UnequipItemContext(mobile, item)).flowSucceeded());
    }

    private UOItem item(int serial, Layer layer, ItemResistances resistances) {
        var data = new UOItemData();
        data.setSerialId(serial);
        data.setLayer(layer);
        data.setFlags(List.of(ItemFlag.WEARABLE));
        data.setLocationType(ItemLocationType.ORPHAN);
        data.setResistances(resistances);
        var item = new UOItem(data, null);
        item.setCurrentLocation(ItemLocation.orphan());
        return item;
    }

    private void assertResistances(UOMobile mobile, int physical, int fire, int cold, int poison, int energy) {
        assertEquals(physical, mobile.getPhysicalResist());
        assertEquals(fire, mobile.getFireResist());
        assertEquals(cold, mobile.getColdResist());
        assertEquals(poison, mobile.getPoisonResist());
        assertEquals(energy, mobile.getEnergyResist());
    }
}
