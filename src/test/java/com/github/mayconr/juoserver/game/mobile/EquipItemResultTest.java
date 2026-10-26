package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.mobile.flow.equip.*;
import com.github.mayconr.juoserver.game.mobile.flow.mount.*;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.ItemEquipped;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipItemResultTest {
    private final EventBus events = mock(EventBus.class);
    private final RealmStorage storage = mock(RealmStorage.class);
    private final DefaultFlowRegistry registry = new DefaultFlowRegistry();
    private final GameInfra infra = GameInfra.builder().storage(storage).eventBus(events).build();
    private final MobileModuleImpl module = new MobileModuleImpl(null, null, events, storage);

    private UOPlayer player() {
        var data = new UOMobileData();
        data.setSerialId(42);
        return new UOPlayer(data);
    }

    private UOItem item(Layer layer) {
        var data = new UOItemData();
        data.setSerialId(0x40000001);
        data.setLayer(layer);
        data.setLocationType(ItemLocationType.ORPHAN);
        return new UOItem(data, null);
    }

    private void initialize() {
        registry.register("equip", EquipItemFlowDefinition.build(infra), EquipItemContext.class);
        module.initialize(DefaultModuleContext.builder()
                .flowFacade(DefaultFlowFacade.builder().registry(registry).build()).build());
    }

    @Test
    void successfulEquipmentReturnsTrueAndUpdatesState() {
        initialize();
        var player = player();
        var item = item(Layer.ONE_HANDED);

        assertTrue(module.equipItem(player, item));
        assertTrue(player.isItemEquipped(item));
        assertEquals(new EquippedLocation(42), item.getCurrentLocation());
        verify(events).publish(new ItemEquipped(player, item));
    }

    @Test
    void mountCompletesUsingRealEquipmentFlow() {
        initialize();
        var player = player();
        var npc = new UONpc(new UOMobileData());
        npc.setName("horse");
        var mountItem = item(Layer.MOUNT);
        var items = mock(ItemModule.class);
        when(items.createItem(any(), any())).thenReturn(mountItem);
        var npcs = mock(NpcModule.class);
        var mounts = new InMemoryTemplateRegistry<>(
                List.of(new MountTemplate("horse_mount", "horse", "horse_item")), MountTemplate::npcName);
        var settings = mock(GamePlaySettings.class);
        when(settings.mobile()).thenReturn(new GamePlaySettings.Mobile("backpack", 2));
        registry.register("mount", MountFlowDefinition.build(
                GameModules.builder().item(items).mobile(module).npc(npcs).build(),
                GameInfra.builder().settings(settings).build(), GameTemplates.builder().mountByNpcName(mounts).build()),
                MountContext.class);

        assertDoesNotThrow(() -> module.mount(player, npc));
        assertTrue(player.isMounted());
        verify(events).publish(new ItemEquipped(player, mountItem));
    }

    @Test
    void failureAfterApplyingEquipmentDoesNotReturnSuccess() {
        initialize();
        var player = player();
        var item = item(Layer.ONE_HANDED);
        doThrow(new IllegalStateException("Notification failed")).when(events).publish(any(ItemEquipped.class));

        assertFalse(module.equipItem(player, item));
    }
}
