package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.mobile.flow.death.spawn.SpawnMobileMountStep;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.npc.NpcRequester;
import com.github.mayconr.juoserver.game.npc.WorldNpc;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpawnMobileMountStepTest {
    private final WorldNpc npcs = mock(WorldNpc.class);
    private final InMemoryTemplateRegistry<String, MountTemplate> mounts = new InMemoryTemplateRegistry<>(
            List.of(new MountTemplate("horse_mount", "horse", "horse_item")), MountTemplate::itemName);

    private DeathContext mountedContext() {
        var victim = new UOMobile(new UOMobileData());
        victim.setX(100);
        victim.setY(200);
        victim.setZ(5);
        var context = new DeathContext(victim, null, null);
        var item = mock(UOItem.class);
        when(item.getName()).thenReturn("horse_item");
        context.setMountItem(item);
        return context;
    }

    @Test
    void recreatesHorseAtVictimPositionEvenAfterMountLayerWasRemoved() {
        var context = mountedContext();
        assertFalse(context.getVictim().isMounted());
        var requester = new NpcRequester.AsyncProcess("death-unmount");
        when(npcs.createNpc(requester, "horse", context.getVictim())).thenReturn(mock(UONpc.class));

        var result = new SpawnMobileMountStep(npcs, mounts).execute(context);

        assertTrue(result.flowSucceeded());
        verify(npcs).createNpc(requester, "horse", context.getVictim());
        assertNull(context.getMountItem());
        new SpawnMobileMountStep(npcs, mounts).execute(context);
        verifyNoMoreInteractions(npcs);
    }

    @Test
    void skipsVictimWithoutMount() {
        var context = new DeathContext(mock(UOMobile.class), null, null);

        assertTrue(new SpawnMobileMountStep(npcs, mounts).execute(context).flowSucceeded());
        verifyNoInteractions(npcs);
    }

    @Test
    void missingMappingDoesNotInterruptDeathNotifications() {
        var context = mountedContext();
        when(context.getMountItem().getName()).thenReturn("unknown_mount");

        var result = new SpawnMobileMountStep(npcs, mounts).execute(context);

        assertEquals("MOUNT_TEMPLATE_NOT_FOUND", result.code());
        assertTrue(result.shouldContinue());
        verifyNoInteractions(npcs);
    }

    @Test
    void failedNpcCreationDoesNotInterruptDeathNotifications() {
        var result = new SpawnMobileMountStep(npcs, mounts).execute(mountedContext());

        assertEquals("MOUNT_CREATION_FAILED", result.code());
        assertTrue(result.shouldContinue());
    }
}
