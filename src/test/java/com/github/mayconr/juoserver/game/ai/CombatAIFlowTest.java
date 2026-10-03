package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIDefinition;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIState;
import com.github.mayconr.juoserver.game.ai.engine.AIEngineImpl;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CombatAIFlowTest {
    @Test
    void combatNpcTriggersRegisteredSkeletonThroughSessionAfterIntervalAndActivation() {
        var registry = new DefaultFlowRegistry();
        registry.register("CombatAIDefinition", CombatAIDefinition.build(), CombatAIContext.class);
        var flows = spy(new DefaultFlowFacade(registry));
        var active = new AtomicBoolean(false);
        var world = mock(World.class, RETURNS_DEEP_STUBS);
        var engine = new AIEngineImpl(world, action -> fail("Skeleton must not dispatch actions"),
                context -> active.get(), (context, speech) -> false, new GamePlaySettings.Ai(0.25));
        engine.initialize(flows);
        var npc = mock(UONpc.class, RETURNS_DEEP_STUBS);
        when(npc.getBehavior()).thenReturn(new com.github.mayconr.juoserver.game.model.BehaviorDefinition(
                "COMBAT", 24, 3, null, java.util.List.of()));
        when(world.storage().getMobilesInRange(eq(npc), eq(3), any())).thenReturn(java.util.List.of());
        engine.attach(npc);

        engine.update(0.25);
        verifyNoInteractions(flows);
        active.set(true);
        engine.update(0.125);
        verifyNoInteractions(flows);
        engine.update(0.125);

        var captured = ArgumentCaptor.forClass(CombatAIContext.class);
        verify(flows).execute(captured.capture());
        var context = captured.getValue();
        assertSame(npc, context.npc());
        assertEquals(CombatAIState.IDLE, context.getState());
        assertNull(context.getTarget());
        assertTrue(context.getNearbyPlayers().isEmpty());
        assertTrue(context.actions().isEmpty());
        assertEquals(8, context.trace().entries().size());
        verify(world.storage()).getMobilesInRange(eq(npc), eq(3), any());
    }
}
