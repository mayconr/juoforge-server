package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.mobile.npc.NpcDespawnService;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NpcRemovalRequesterTest {
    @Test
    void passesPlayerAndProcessToRemovalFlow() {
        var flows = mock(ModuleContext.FlowFacade.class);
        var moduleContext = mock(ModuleContext.class);
        when(moduleContext.flows()).thenReturn(flows);
        var module = new NpcModuleImpl();
        module.initialize(moduleContext);
        var npc = mock(UONpc.class);
        var requesters = List.of(new NpcRequester.Player(mock(UOPlayer.class)),
                new NpcRequester.AsyncProcess("cleanup"));

        for (var requester : requesters) {
            module.removeNpc(requester, npc);
        }

        var contexts = ArgumentCaptor.forClass(NpcRemovalContext.class);
        verify(flows, times(2)).execute(contexts.capture());
        for (int i = 0; i < requesters.size(); i++) {
            assertSame(requesters.get(i), contexts.getAllValues().get(i).getRequester());
            assertSame(npc, contexts.getAllValues().get(i).getNpc());
        }
        assertThrows(NullPointerException.class, () -> module.removeNpc(null, npc));
    }

    @Test
    void expiredDeadNpcUsesNamedProcessAndRemovalModule() {
        var storage = mock(RealmStorage.class);
        var module = mock(NpcModule.class);
        var service = new NpcDespawnService(storage, module);
        var npc = mock(UONpc.class);
        when(npc.getEquippedItems()).thenReturn(Map.of());
        when(npc.getBackpack()).thenReturn(null);
        service.scheduleDespawn(npc, 1);

        service.update(1);
        service.update(1);

        verify(module).removeNpc(new NpcRequester.AsyncProcess("npc-despawn"), npc);
        verify(storage, never()).deleteMobile(npc);
    }
}
