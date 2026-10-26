package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.flow.mount.*;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.*;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MountOperationTest {
    @ParameterizedTest
    @CsvSource({"1, true, true", "3, true, false", "1, false, false"})
    void returnsCompletedStatusOrFlowFailureMessage(int distance, boolean equipSucceeds, boolean expected) {
        var player = new UOPlayer(new UOMobileData());
        var npc = new UONpc(new UOMobileData());
        npc.setName("horse");
        npc.setX(distance);
        var item = mock(UOItem.class);
        var items = mock(ItemModule.class);
        when(items.createItem(any(), any())).thenReturn(item);
        var mobiles = mock(MobileModule.class);
        when(mobiles.equipItem(player, item)).thenReturn(equipSucceeds);
        var npcs = mock(NpcModule.class);
        var mounts = new InMemoryTemplateRegistry<>(
                List.of(new MountTemplate("horse_mount", "horse", "horse_item")), MountTemplate::npcName);
        var settings = mock(GamePlaySettings.class);
        when(settings.mobile()).thenReturn(new GamePlaySettings.Mobile("backpack", 2));
        var flow = MountFlowDefinition.build(GameModules.builder().item(items).mobile(mobiles).npc(npcs).build(),
                GameInfra.builder().settings(settings).build(), GameTemplates.builder().mountByNpcName(mounts).build());
        var registry = new DefaultFlowRegistry();
        registry.register("mount", flow, MountContext.class);
        var module = new MobileModuleImpl(null, null, null, null);
        module.initialize(DefaultModuleContext.builder()
                .flowFacade(DefaultFlowFacade.builder().registry(registry).build()).build());

        if (expected) {
            assertDoesNotThrow(() -> module.mount(player, npc));
        } else {
            var error = assertThrows(ModuleOperationException.class, () -> module.mount(player, npc));
            assertEquals(distance > 2 ? "Mount is outside the allowed range of 2" : "Cannot equip mount item",
                    error.getMessage());
            assertEquals(distance > 2 ? "{mount.out_of_range}" : "{mount.equip_failed}", error.getDisplayMessage());
            assertEquals("mount", error.getOperation());
        }
        if (distance > 2) verifyNoInteractions(items, npcs, mobiles);
    }

    @Test
    void failuresWithoutDisplayMessageUseDefaultMountKey() {
        var flows = mock(ModuleContext.FlowFacade.class);
        when(flows.execute(any(MountContext.class))).thenReturn(StepResult.failure("Unexpected failure"));
        var module = new MobileModuleImpl(null, null, null, null);
        module.initialize(DefaultModuleContext.builder().flowFacade(flows).build());

        var error = assertThrows(ModuleOperationException.class, () -> module.mount(null, null));
        assertEquals("Unexpected failure", error.getMessage());
        assertEquals("{mount.failed}", error.getDisplayMessage());
    }
}
