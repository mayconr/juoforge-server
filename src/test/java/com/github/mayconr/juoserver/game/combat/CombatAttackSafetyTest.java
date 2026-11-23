package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionFlow;
import com.github.mayconr.juoserver.game.combat.flow.execution.damage.ApplyDamageStep;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.game.damage.DamageModule;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.SkillContainer;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.CombatOccurring;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.support.TestFields;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CombatAttackSafetyTest {
    @Test
    void runningTargetCanBeHitDuringBriefContactAndCannotBeHitTwiceDuringCooldown() {
        var attacker = mock(UONpc.class);
        var target = mock(UOPlayer.class);
        var targetX = new AtomicInteger(2);
        when(attacker.isAlive()).thenReturn(true);
        when(attacker.getStrength()).thenReturn(50);
        when(attacker.getSkills()).thenReturn(new SkillContainer());
        when(target.isAlive()).thenReturn(true);
        when(target.isConnected()).thenReturn(true);
        when(target.getSerialId()).thenReturn(2);
        when(target.getSkills()).thenReturn(new SkillContainer());
        when(target.getX()).thenAnswer(invocation -> targetX.get());

        var events = new DefaultEventBus();
        var damage = mock(DamageModule.class);
        var flow = CombatExecutionFlow.build(
                GameModules.builder().skill(mock(SkillModule.class)).damage(damage).build(),
                GameInfra.builder().eventBus(events).rng(chance -> true)
                        .combatSkillGainPolicy(context -> List.of()).build());
        var flows = mock(ModuleContext.FlowFacade.class);
        when(flows.execute(any(CombatExecutionContext.class)))
                .thenAnswer(invocation -> flow.execute(invocation.getArgument(0)));
        var module = module(flows);
        var registry = (CombatSessionRegistry) TestFields.getField(module, "registry");
        registry.register(new CombatSession(UUID.randomUUID(), attacker, target, new CombatSession.PhysicalTrigger()));
        events.register(MobileMoved.class, module::onMobileMoved);
        events.register(CombatOccurring.class, event -> targetX.set(2));

        var entersReach = new MobileMoved(target, Direction.WEST,
                new PointInTheWorld(2, 0, 0), new PointInTheWorld(1, 0, 0), true, 1, false);
        module.update(0.25);
        verifyNoInteractions(damage);

        targetX.set(1);
        events.publish(entersReach);
        verify(damage).applyDamage(any());

        targetX.set(1);
        events.publish(entersReach);
        verify(damage).applyDamage(any());

        module.update(1);
        verify(damage, times(2)).applyDamage(any());
    }

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
        var module = new CombatModuleImpl();
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
        var registry = (CombatSessionRegistry) TestFields.getField(module, "registry");
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
