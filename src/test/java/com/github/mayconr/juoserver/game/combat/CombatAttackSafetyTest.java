package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.flow.execution.damage.ApplyDamageStep;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.game.damage.DamageModule;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CombatAttackSafetyTest {
    @Test
    void validNpcSessionAppliesDamage() {
        var attacker = mock(UONpc.class);
        var target = mock(UOPlayer.class);
        when(attacker.isAlive()).thenReturn(true);
        when(target.isAlive()).thenReturn(true);
        when(target.isConnected()).thenReturn(true);
        var session = new CombatSession(UUID.randomUUID(), attacker, target, new CombatSession.PhysicalTrigger());
        var context = new CombatExecutionContext(session);
        context.setCombatRadius(1);
        var damage = mock(DamageModule.class);
        assertTrue(new ApplyDamageStep(damage).execute(context).shouldContinue());
        verify(damage).applyDamage(any());
        assertTrue(session.isActive());
    }

    private CombatModuleImpl module(ModuleContext.FlowFacade flows) {
        var module = new CombatModuleImpl(mock(CombatHandler.class), mock(VitalsHandler.class));
        var context = mock(ModuleContext.class);
        when(context.flows()).thenReturn(flows);
        module.initialize(context);
        return module;
    }

    @Test
    void repeatedNpcRequestsRetainOneActiveSessionAndTargetChangeClosesOldSession() {
        var flows = mock(ModuleContext.FlowFacade.class);
        var module = module(flows);
        var npc = mock(UONpc.class);
        when(npc.getSerialId()).thenReturn(1);
        doAnswer(inv -> {
            CombatPreparationContext context = inv.getArgument(0);
            var target = mock(UOPlayer.class);
            when(target.getSerialId()).thenReturn(context.getTargetSerial());
            context.setSession(new CombatSession(UUID.randomUUID(), npc, target, new CombatSession.PhysicalTrigger()));
            return null;
        }).when(flows).execute(any(CombatPreparationContext.class));
        for (int i = 0; i < 5; i++) module.requestAttack(npc, 42);
        module.update(1);
        var registry = (CombatSessionRegistry) org.springframework.test.util.ReflectionTestUtils.getField(module, "registry");
        var first = registry.getByPlayer(npc);
        module.requestAttack(npc, 42);
        module.update(1);
        assertSame(first, registry.getByPlayer(npc));
        verify(flows).execute(any(CombatPreparationContext.class));
        module.requestAttack(npc, 43);
        module.update(1);
        assertFalse(first.isActive());
        assertEquals(1, registry.getSessions().size());
        assertEquals(43, registry.getByPlayer(npc).getTarget().getSerialId());
        verify(flows, times(2)).execute(any(CombatPreparationContext.class));
    }

    @Test
    void cancelSuppressesPendingAttackAndNewIdenticalRequestIsProcessedAfterCancellation() {
        var flows = mock(ModuleContext.FlowFacade.class);
        var module = module(flows);
        var npc = mock(UONpc.class);
        module.requestAttack(npc, 42);
        module.requestCancelAttack(npc);
        module.update(1);
        verifyNoInteractions(flows);
        module.requestAttack(npc, 42);
        module.requestCancelAttack(npc);
        module.requestAttack(npc, 42);
        module.update(1);
        verify(flows).execute(any(CombatPreparationContext.class));
    }

    @Test
    void damageIsPreventedAfterCancellationDeathDisconnectOrMovingOutOfRange() {
        var attacker = mock(UONpc.class);
        var target = mock(UOPlayer.class);
        when(attacker.isAlive()).thenReturn(true);
        when(target.isAlive()).thenReturn(true);
        when(target.isConnected()).thenReturn(true);
        var damage = mock(DamageModule.class);
        var step = new ApplyDamageStep(damage);
        for (int scenario = 0; scenario < 4; scenario++) {
            when(target.isAlive()).thenReturn(true);
            when(target.isConnected()).thenReturn(true);
            when(target.getX()).thenReturn(0);
            var session = new CombatSession(UUID.randomUUID(), attacker, target, new CombatSession.PhysicalTrigger());
            var context = new CombatExecutionContext(session);
            context.setCombatRadius(1);
            switch (scenario) {
                case 0 -> session.close();
                case 1 -> when(target.isAlive()).thenReturn(false);
                case 2 -> when(target.isConnected()).thenReturn(false);
                case 3 -> when(target.getX()).thenReturn(5);
            }
            assertTrue(step.execute(context).shouldStop());
        }
        verifyNoInteractions(damage);
    }
}
