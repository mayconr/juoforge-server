package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.item.ItemModuleImpl;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.flow.creation.notification.NotifyItemCreatedStep;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceEquippedItemStep;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemFlowDefinition;
import com.github.mayconr.juoserver.game.model.EquipItemTarget;
import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.event.EquippedItemCreated;
import com.github.mayconr.juoserver.game.model.event.ItemEquipped;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.network.packet.DrawMobile;
import com.github.mayconr.juoserver.network.packet.EquipItem;
import com.github.mayconr.juoserver.network.session.handler.ItemEventHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreatedEquipmentTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void creationSendsEquipmentAfterTheItemIsIntroducedOnlyWhenRendering(boolean render) {
        var channel = new EmbeddedChannel();
        try {
            var mobile = new UOMobile(new UOMobileData());
            var data = new UOItemData();
            data.setSerialId(0x40000001);
            data.setLayer(Layer.ONE_HANDED);
            data.setLocationType(ItemLocationType.ORPHAN);
            var item = new UOItem(data, null);
            var events = new DefaultEventBus();
            var storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
            var registry = new DefaultFlowRegistry();
            registry.register("equip", EquipItemFlowDefinition.build(
                    GameInfra.builder().storage(storage).eventBus(events).build(), mock(WorldMobile.class)),
                    EquipItemContext.class);
            var items = new ItemModuleImpl(storage, null, null);
            items.initialize(DefaultModuleContext.builder()
                    .flowFacade(DefaultFlowFacade.builder().registry(registry).build()).build());
            var view = mock(SessionView.class);
            when(view.shouldReceiveUpdate(mobile)).thenReturn(true);
            var world = mock(WorldInternal.class, RETURNS_DEEP_STUBS);
            when(world.mobile().getEquippedItems(mobile)).thenReturn(Map.of(item.getLayer(), item));
            var handler = new ItemEventHandler(view, new SessionPacketWriter(channel, null), world);
            events.register(EquippedItemCreated.class, handler::onEquippedItemCreated);
            events.register(ItemEquipped.class, handler::onItemEquipped);
            var context = new ItemCreationContext(ItemRequest.byName("sword"), new EquipItemTarget(mobile));
            context.setOptions(new ItemCreationOptions().renderOnCreate(render));
            context.complete(item);

            assertTrue(new PlaceEquippedItemStep(items).execute(context).flowSucceeded());
            assertTrue(mobile.isItemEquipped(item));
            channel.runPendingTasks();
            assertNull(channel.readOutbound());

            assertTrue(new NotifyItemCreatedStep(events).execute(context).flowSucceeded());
            channel.runPendingTasks();
            if (render) {
                assertInstanceOf(DrawMobile.class, channel.readOutbound());
                assertInstanceOf(EquipItem.class, channel.readOutbound());
            }
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
