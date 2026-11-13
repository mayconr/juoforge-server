package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.item.ItemModuleImpl;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.decay.ItemDecayState;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemFlowDefinition;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemMapper;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.property.PropertyStorage;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import com.github.mayconr.juoserver.network.packet.DropItem;
import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroundDropSaveTest {
    @Test
    void groundDropPropertyReachesItemStorageOnWorldSave() {
        var backend = mock(ItemStorage.class);
        var mobiles = mock(MobileStorage.class);
        var properties = mock(PropertyStorage.class);
        var savedAttributes = new AtomicReference<Map<String, Object>>();
        var data = new UOItemData();
        data.setSerialId(0x40000001);
        data.setLocationType(ItemLocationType.GROUND);
        data.setAmount(1);
        data.setFlags(List.of());
        data.setPersistentAttrMap(new DefaultAttributeMap());
        data.setX(100);
        data.setY(100);
        when(backend.findAllGroundItems()).thenReturn(CompletableFuture.completedFuture(List.of(data)));
        when(mobiles.findAllNpcs()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(mobiles.saveMobiles(anyInt(), anyCollection(), anyCollection()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));
        when(properties.loadAll()).thenReturn(CompletableFuture.completedFuture(Map.of()));
        when(properties.saveAll(anyMap(), anySet())).thenReturn(CompletableFuture.completedFuture(null));
        when(backend.saveItems(anyInt(), anyCollection(), anyCollection())).thenAnswer(call -> {
            Collection<UOItemData> updated = call.getArgument(1);
            savedAttributes.set(updated.iterator().next().getPersistentAttrMap().toMap());
            return CompletableFuture.completedFuture(updated);
        });
        var templates = new InMemoryTemplateRegistry<String, ItemTemplate>(List.of(), ItemTemplate::name);
        var storage = new CachedWorldStorage(mobiles, backend, null, new ItemMapper(templates), properties);
        storage.initialize(() -> 1, () -> 1, initial -> { });
        var items = new ItemModuleImpl(storage, null, null);
        var registry = new DefaultFlowRegistry();
        registry.register("drop", DropItemFlowDefinition.build(
                GameModules.builder().item(items).build(),
                GameInfra.builder().storage(storage).eventBus(mock(EventBus.class)).build()
        ), DropItemContext.class);
        items.initialize(DefaultModuleContext.builder().flowFacade(new DefaultFlowFacade(registry)).build());
        var player = mock(UOPlayer.class);
        when(player.getSerialId()).thenReturn(1);
        when(player.getX()).thenReturn(100);
        when(player.getY()).thenReturn(100);
        var item = storage.items().get(data.getSerialId()).orElseThrow();
        items.heldItems().hold(player, item);
        var buffer = Unpooled.buffer();
        buffer.writeByte(0x08).writeInt(item.getSerialId()).writeShort(101).writeShort(100)
                .writeByte(0).writeByte(0).writeInt(-1);
        try {
            items.dropItem(player, new DropItem(buffer));
        } finally {
            buffer.release();
        }
        var droppedAt = items.getProperty(item, ItemDecayState.DROPPED_AT, false);
        assertNotNull(droppedAt);
        storage.saveWorld().join();
        assertEquals(droppedAt, savedAttributes.get().get(ItemDecayState.DROPPED_AT));
    }
}
