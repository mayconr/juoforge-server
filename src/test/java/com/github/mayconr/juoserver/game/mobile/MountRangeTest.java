package com.github.mayconr.juoserver.game.mobile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.mobile.flow.mount.*;
import com.github.mayconr.juoserver.game.mobile.flow.mount.validation.ValidateMountStep;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.*;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MountRangeTest {
    private final InMemoryTemplateRegistry<String, MountTemplate> mounts = new InMemoryTemplateRegistry<>(
            List.of(new MountTemplate("horse_mount", "horse", "horse_item")), MountTemplate::npcName);

    private MountContext context(int x, int y, int z) {
        var player = new UOPlayer(new UOMobileData());
        var npc = new UONpc(new UOMobileData());
        npc.setName("horse");
        npc.setX(x);
        npc.setY(y);
        npc.setZ(z);
        return new MountContext(player, npc);
    }

    @Test
    void defaultsRangeWhenOmittedAndAcceptsConfigurationOverride() throws Exception {
        var mapper = new ObjectMapper();
        assertEquals(2, mapper.readValue("{\"backpackItem\":\"backpack\"}", GamePlaySettings.Mobile.class).mountRange());
        assertEquals(4, mapper.readValue("{\"backpackItem\":\"backpack\",\"mountRange\":4}", GamePlaySettings.Mobile.class).mountRange());
        assertThrows(IllegalArgumentException.class, () -> new GamePlaySettings.Mobile("backpack", -1));
    }

    @ParameterizedTest
    @CsvSource({"2, 2, 8, 2, true", "3, 0, 0, 2, false", "0, 0, 9, 2, false",
            "3, 3, 0, 3, true", "0, 0, 0, 0, true", "1, 0, 0, 0, false"})
    void validatesConfiguredRangeAndElevation(int x, int y, int z, int range, boolean allowed) {
        var result = new ValidateMountStep(mounts, range).execute(context(x, y, z));
        assertEquals(allowed, result.flowSucceeded());
        if (!allowed) assertEquals("MOUNT_OUT_OF_RANGE", result.code());
    }

    @Test
    void outOfRangeStopsFlowBeforeCreatingItemOrRemovingNpc() {
        var items = mock(ItemModule.class);
        var npcs = mock(NpcModule.class);
        var mobiles = mock(MobileModule.class);
        var settings = mock(GamePlaySettings.class);
        when(settings.mobile()).thenReturn(new GamePlaySettings.Mobile("backpack", 2));
        var flow = MountFlowDefinition.build(GameModules.builder().item(items).npc(npcs).mobile(mobiles).build(),
                GameInfra.builder().settings(settings).build(), GameTemplates.builder().mountByNpcName(mounts).build());

        assertTrue(flow.execute(context(3, 0, 0)).flowFailed());
        verifyNoInteractions(items, npcs, mobiles);
    }
}
