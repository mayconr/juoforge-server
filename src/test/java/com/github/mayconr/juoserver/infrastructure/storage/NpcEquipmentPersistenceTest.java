package com.github.mayconr.juoserver.infrastructure.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.item.ItemModuleImpl;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationFlowDefinition;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.mobile.template.NpcStatProfile;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.npc.NpcModuleImpl;
import com.github.mayconr.juoserver.game.npc.NpcRequester;
import com.github.mayconr.juoserver.game.npc.ai.NpcAIProfileResolver;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationFlowDefinition;
import com.github.mayconr.juoserver.game.npc.stats.NpcStatsResolver;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercises the library's public creation API and persistence lifecycle with storage SPI test doubles. */
class NpcEquipmentPersistenceTest {
    @ParameterizedTest(name = "startup={0}, equipped={1}")
    @CsvSource({"true,true", "false,true", "true,false", "false,false"})
    void equipmentSurvivesSaveAndFreshStorage(boolean startup, boolean equipped) throws Exception {
        var json = new ObjectMapper();
        var sword = json.readValue("""
                {"name":"sword","modelId":5049,"layer":"ONE_HANDED","hue":42}
                """, ItemTemplate.class);
        var backpack = json.readValue("""
                {"name":"backpack","modelId":3701,"layer":"BACKPACK","flags":["CONTAINER"]}
                """, ItemTemplate.class);
        var npcTemplate = json.readValue("""
                {"name":"guard","statProfile":"basic","behavior":{"ai":"passive"},
                 "equippedItems":%s}
                """.formatted(equipped ? "[\"sword\",\"backpack\"]" : "[]"), NpcTemplate.class);
        var names = new InMemoryTemplateRegistry<>(List.of(sword, backpack), ItemTemplate::name);
        var mapper = new ItemMapper(names);
        var mobiles = mock(MobileStorage.class);
        var items = mock(ItemStorage.class);
        var savedMobiles = new HashMap<Integer, UOMobileData>();
        var savedItems = new HashMap<Integer, UOItemData>();

        when(items.findNextItemSerial()).thenReturn(CompletableFuture.completedFuture(0x40000000));
        when(mobiles.findNextMobileSerial()).thenReturn(CompletableFuture.completedFuture(1));
        when(mobiles.saveMobiles(anyInt(), anyCollection(), anyCollection())).thenAnswer(call -> {
            Collection<UOMobileData> data = call.getArgument(1);
            for (var mobile : data) {
                // Equipment must be rebuilt from item ownership, as with an independent storage adapter.
                mobile.setEquippedItems(Map.of());
                savedMobiles.put(mobile.getSerialId(), mobile);
            }
            return CompletableFuture.completedFuture(data);
        });
        when(items.saveItems(anyInt(), anyCollection(), anyCollection())).thenAnswer(call -> {
            Collection<UOItemData> data = call.getArgument(1);
            data.forEach(item -> savedItems.put(item.getSerialId(), item));
            return CompletableFuture.completedFuture(data);
        });
        when(mobiles.findAllNpcs()).thenAnswer(call -> CompletableFuture.completedFuture(List.copyOf(savedMobiles.values())));
        when(mobiles.findMobileBySerialId(anyInt())).thenAnswer(call ->
                CompletableFuture.completedFuture(savedMobiles.get(call.getArgument(0))));
        when(mobiles.findSkillsBySerialId(anyInt())).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.findAllGroundItems()).thenAnswer(call -> CompletableFuture.completedFuture(savedItems.values().stream()
                .filter(item -> item.getOwnerSerialId() == null && item.getContainerSerialId() == null).toList()));
        when(items.findAllEquippedItems(anyInt())).thenAnswer(call -> CompletableFuture.completedFuture(savedItems.values().stream()
                .filter(item -> Objects.equals(item.getOwnerSerialId(), call.getArgument(0))
                        && item.getContainerSerialId() == null).toList()));

        var storage = new CachedRealmStorage(mobiles, items, null, mapper);
        var serials = new SerialGenerator(storage);
        serials.initialize();
        initialize(storage, serials);
        var infra = GameInfra.builder().storage(storage).serialGenerator(serials).eventBus(mock(EventBus.class)).build();
        var templates = GameTemplates.builder().itemByName(names)
                .itemByModelId(new InMemoryTemplateRegistry<>(List.of(sword, backpack), ItemTemplate::modelId))
                .npcByName(new InMemoryTemplateRegistry<>(List.of(npcTemplate), NpcTemplate::name))
                .npcStatsResolver(new NpcStatsResolver(List.of(new NpcStatProfile("basic", 10, 10, 10, 10, 10, 10))))
                .npcAIProfileResolver(new NpcAIProfileResolver(List.of())).build();
        var registry = new DefaultFlowRegistry();
        var context = DefaultModuleContext.builder()
                .flowFacade(DefaultFlowFacade.builder().registry(registry).build()).build();
        var itemModule = new ItemModuleImpl(null, storage, null, null);
        itemModule.initialize(context);
        registry.register("create-item", ItemCreationFlowDefinition.build(infra, templates, new com.github.mayconr.juoserver.game.container.ContainerModuleImpl(storage, new com.github.mayconr.juoserver.game.item.ContainerHandler(infra.eventBus(), storage)), mock(com.github.mayconr.juoserver.game.mobile.WorldMobile.class)), ItemCreationContext.class);
        registry.register("create-npc", NpcCreationFlowDefinition.build(
                GameModules.builder().item(itemModule).ai(mock(AIModule.class)).build(), infra, templates), NpcCreationContext.class);
        var npcModule = new NpcModuleImpl();
        npcModule.initialize(context);

        var npc = npcModule.createNpc(new NpcRequester.AsyncProcess("test"), "guard", mock(Location.class));
        assertNotNull(npc);
        var equipment = Map.copyOf(npc.getEquippedItems());
        assertEquals(equipped ? 2 : 0, equipment.size());
        storage.saveMobiles().join();
        storage.saveItems().join();
        assertEquals(equipment.size(), savedItems.size());
        for (var item : savedItems.values()) {
            assertEquals(ItemLocationType.EQUIPPED, item.getLocationType());
            assertEquals(npc.getSerialId(), item.getOwnerSerialId());
            assertNull(item.getContainerSerialId());
        }

        // Discard live state; a fresh cache must reconstruct the association from saved DTOs.
        equipment.values().forEach(serial -> storage.getItem(serial).orElseThrow().setCurrentLocation(ItemLocation.orphan()));
        npc.getEquippedItems().clear();
        if (!startup) {
            when(mobiles.findAllNpcs()).thenReturn(CompletableFuture.completedFuture(List.of()));
        }
        var reloadedStorage = new CachedRealmStorage(mobiles, items, null, mapper);
        var initial = initialize(reloadedStorage, serials);
        assertTrue(initial.items().isEmpty(), "Equipment must not reload as ground items");
        UOMobile reloaded;
        if (startup) {
            reloaded = reloadedStorage.getMobile(npc.getSerialId()).orElseThrow();
        } else {
            reloaded = reloadedStorage.loadMobile(npc.getSerialId()).join();
        }
        assertNotSame(npc, reloaded);
        assertEquals(equipment, reloaded.getEquippedItems());
        for (var entry : equipment.entrySet()) {
            var item = reloadedStorage.getItem(entry.getValue()).orElseThrow();
            assertEquals(entry.getKey(), item.getLayer());
            assertEquals(ItemLocation.equipped(reloaded.getSerialId()), item.getCurrentLocation());
            assertEquals(savedItems.get(item.getSerialId()).getModelId(), item.getModelId());
            assertEquals(savedItems.get(item.getSerialId()).getHue(), item.getHue());
        }
        if (equipped) {
            assertEquals(equipment.get(Layer.BACKPACK), reloaded.getBackpack());
            assertInstanceOf(UOContainer.class, reloadedStorage.getItem(reloaded.getBackpack()).orElseThrow());
        }
    }

    private InitialData initialize(CachedRealmStorage storage, SerialGenerator serials) throws Exception {
        var initialized = new CompletableFuture<InitialData>();
        storage.initialize(serials::getCurrentItem, serials::getCurrentMobile, initialized::complete);
        return initialized.get(5, TimeUnit.SECONDS);
    }
}
