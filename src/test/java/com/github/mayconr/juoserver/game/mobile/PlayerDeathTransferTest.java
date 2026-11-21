package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.item.ItemCreationResult;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.flow.gold.RecalculateGoldContext;
import com.github.mayconr.juoserver.game.mobile.flow.gold.RecalculateGoldFlowDefinition;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.death.transfer.MoveItemsToCorpseStep;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.DeathCause;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemResistances;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.game.model.event.MobileGoldChanged;
import com.github.mayconr.juoserver.game.model.event.MobileResistancesChanged;
import com.github.mayconr.juoserver.game.model.event.MobileResistancesChanged.Values;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerDeathTransferTest {
    private UOItemData data(int serial, Layer layer, ItemLocationType location, List<ItemFlag> flags) {
        var data = new UOItemData();
        data.setSerialId(serial);
        data.setLayer(layer);
        data.setMovable(false); // Death transfer must also handle explicitly immovable items.
        data.setOwnerSerialId(1);
        data.setContainerSerialId(100);
        data.setLocationType(location);
        data.setFlags(flags);
        data.setPersistentAttrMap(new DefaultAttributeMap());
        return data;
    }

    @Test
    void fullDeathMovesImmovableWeaponAndUnopenedBackpackContentsBeforeNotifying() {
        var mobileData = new UOMobileData();
        mobileData.setSerialId(1);
        mobileData.setPhysicalResist(5);
        var player = new UOPlayer(mobileData);
        player.setAlive(true);
        player.setGold(100);
        var backpack = new UOContainer(data(100, Layer.BACKPACK, ItemLocationType.EQUIPPED,
                List.of(ItemFlag.CONTAINER, ItemFlag.WEARABLE)), null);
        player.setBackpack(backpack);
        var weaponData = data(101, Layer.ONE_HANDED, ItemLocationType.EQUIPPED,
                List.of(ItemFlag.WEARABLE, ItemFlag.WEAPON));
        weaponData.setResistances(new ItemResistances(5, 0, 0, 0, 0));
        var weapon = new UOItem(weaponData, null);
        player.getEquippedItems().put(Layer.ONE_HANDED, weapon.getSerialId());
        var gold = new UOItem(data(102, null, ItemLocationType.CONTAINER, List.of(ItemFlag.STACKABLE)), null);
        gold.setName("gold_coin");
        gold.setAmount(100);
        gold.setCurrentLocation(new ContainerLocation(backpack.getSerialId()));
        var nested = new UOContainer(data(103, null, ItemLocationType.CONTAINER, List.of(ItemFlag.CONTAINER)), null);
        nested.setCurrentLocation(new ContainerLocation(backpack.getSerialId()));
        var nestedGold = new UOItem(data(104, null, ItemLocationType.CONTAINER, List.of(ItemFlag.STACKABLE)), null);
        nestedGold.setCurrentLocation(new ContainerLocation(nested.getSerialId()));
        nested.addItemToContainer(nestedGold);
        var corpse = new UOCorpse(data(200, null, ItemLocationType.GROUND, List.of(ItemFlag.CORPSE)), null);
        var storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
        var cache = Map.of(100, backpack, 101, weapon, 102, gold, 103, nested, 104, nestedGold);
        when(storage.items().get(anyInt())).thenAnswer(call -> Optional.ofNullable(cache.get(call.getArgument(0))));
        when(storage.items().getContainer(100)).thenReturn(Optional.of(backpack));
        when(storage.mobiles().get(player.getSerialId())).thenReturn(Optional.of(player));
        when(storage.items().loadContainerItems(backpack)).thenReturn(CompletableFuture.completedFuture(List.of(gold, nested)));
        var events = mock(EventBus.class);
        var items = mock(ItemModule.class);
        when(items.tryCreateItem(any(), any(), any())).thenReturn(new ItemCreationResult.Created(corpse));
        var wallet = mock(Wallet.class);
        when(wallet.getBalance(player)).thenAnswer(call -> backpack.getContainerItems().contains(gold.getSerialId())
                ? gold.getAmount() : 0);
        var mobile = new MobileModuleImpl(null, wallet, events, storage, new GamePlaySettings());
        var goldRegistry = new DefaultFlowRegistry();
        goldRegistry.register("recalculate-gold",
                RecalculateGoldFlowDefinition.build(wallet, events,
                        mock(WorldUI.class)),
                RecalculateGoldContext.class);
        mobile.initialize(new DefaultModuleContext(
                new DefaultFlowFacade(goldRegistry)));
        doAnswer(call -> {
            assertFalse(player.isAlive());
            assertEquals(0, player.getPhysicalResist());
            assertEquals(0, player.getGold());
            assertTrue(backpack.getContainerItems().isEmpty());
            assertFalse(player.isItemEquipped(weapon));
            return null;
        }).when(events).publish(any(MobileDeathEvent.class));
        assertFalse(weapon.isMovable());
        assertTrue(backpack.getContainerItems().isEmpty()); // Contents have not been opened/loaded yet.
        var flow = DeathFlowDefinition.build(GameModules.builder().mobile(mobile).item(items).build(),
                GameInfra.builder().storage(storage).eventBus(events).build(), GameTemplates.builder().build());
        var result = flow.execute(new DeathContext(player, new UOPlayer(new UOMobileData()), DeathCause.COMBAT));
        assertTrue(result.flowSucceeded());
        assertEquals(Map.of(Layer.BACKPACK, 100), player.getEquippedItems());
        assertEquals(100, player.getBackpack());
        assertEquals(Set.of(101, 102, 103), Set.copyOf(corpse.getContainerItems()));
        assertEquals(Map.of(Layer.ONE_HANDED, 101), corpse.getEquippedItems());
        for (var item : List.of(weapon, gold, nested)) {
            assertEquals(new ContainerLocation(200), item.getCurrentLocation());
            assertEquals(ItemLocationType.CONTAINER, item.toData().getLocationType());
            assertEquals(200, item.toData().getContainerSerialId());
        }
        assertEquals(new ContainerLocation(103), nestedGold.getCurrentLocation());
        assertTrue(nested.getContainerItems().contains(104));
        verify(events).publish(new MobileResistancesChanged(player, new Values(5, 0, 0, 0, 0)));
        var order = inOrder(events);
        order.verify(events).publish(new MobileGoldChanged(player, 100, 0));
        order.verify(events).publish(any(MobileDeathEvent.class));
        verify(storage.items()).loadContainerItems(backpack);
    }

    @Test
    void missingEquipmentFailsBeforeRemovingAnyOtherEquipment() {
        var mobileData = new UOMobileData();
        mobileData.setSerialId(1);
        var player = new UOPlayer(mobileData);
        var first = new UOItem(data(101, Layer.ONE_HANDED, ItemLocationType.EQUIPPED, List.of()), null);
        player.getEquippedItems().put(Layer.ONE_HANDED, 101);
        player.getEquippedItems().put(Layer.TWO_HANDED, 999);
        var storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
        when(storage.items().get(101)).thenReturn(Optional.of(first));
        when(storage.items().get(999)).thenReturn(Optional.empty());
        var context = new DeathContext(player, null, DeathCause.COMBAT);
        var corpse = new UOCorpse(data(200, null, ItemLocationType.GROUND, List.of(ItemFlag.CORPSE)), null);
        context.corpse(corpse);
        var result = FlowFactory.<DeathContext>builder()
                .step(new MoveItemsToCorpseStep(null, null, storage)).build().execute(context);
        assertTrue(result.flowFailed());
        assertEquals(2, player.getEquippedItems().size());
        assertTrue(corpse.getContainerItems().isEmpty());
        assertInstanceOf(EquippedLocation.class, first.getCurrentLocation());
    }
}
