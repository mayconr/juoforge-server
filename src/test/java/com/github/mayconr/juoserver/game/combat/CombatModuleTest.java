package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.network.packet.AttackRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CombatModuleTest {
    @Test
    void publicApiAndPacketAdapterQueueEquivalentAttacks() {
        var module = new CombatModuleImpl(mock(CombatHandler.class), mock(VitalsHandler.class));
        var flows = mock(ModuleContext.FlowFacade.class);
        var context = mock(ModuleContext.class);
        when(context.flows()).thenReturn(flows);
        module.initialize(context);
        var player = mock(UOPlayer.class);
        var packet = mock(AttackRequest.class);
        when(packet.getOpponentSerialId()).thenReturn(42);

        WorldCombat combat = module;
        combat.requestAttack(player, 42);
        module.requestAttack(player, packet);
        verifyNoInteractions(flows);

        module.update(1);

        var requests = ArgumentCaptor.forClass(CombatPreparationContext.class);
        verify(flows, times(2)).execute(requests.capture());
        for (var request : requests.getAllValues()) {
            assertSame(player, request.getAttacker());
            assertEquals(42, request.getTargetSerial());
            assertInstanceOf(CombatPreparationContext.RequestOrigin.class, request.getOrigin());
        }
        module.update(1);
        verifyNoMoreInteractions(flows);
    }
}
