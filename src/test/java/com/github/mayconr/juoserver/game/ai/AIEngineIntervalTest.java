package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.engine.AIEngineImpl;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIEngineIntervalTest {
    private final ModuleContext.FlowFacade flows = mock(ModuleContext.FlowFacade.class);

    private AIEngineImpl engine(double interval) {
        var engine = new AIEngineImpl(mock(World.class), action -> {},
                context -> true, (context, speech) -> false, new GamePlaySettings.Ai(interval));
        engine.initialize(flows);
        var npc = mock(UONpc.class, RETURNS_DEEP_STUBS);
        when(npc.getBehavior().ai()).thenReturn("PASSIVE_ANIMAL");
        engine.attach(npc);
        return engine;
    }

    @Test
    void waitsForIntervalAndPassesAccumulatedTime() {
        var engine = engine(0.25);
        engine.update(0.125);
        verifyNoInteractions(flows);
        doAnswer(invocation -> {
            AIFlowContext context = invocation.getArgument(0);
            assertEquals(0.25, context.delta());
            return null;
        }).when(flows).execute(any(AIFlowContext.class));
        engine.update(0.125);
        verify(flows).execute(any(AIFlowContext.class));
        engine.update(0.125);
        verifyNoMoreInteractions(flows);
    }

    @Test
    void slowTickExecutesOnlyOnceAndPreservesElapsedTime() {
        var engine = engine(0.25);
        doAnswer(invocation -> {
            AIFlowContext context = invocation.getArgument(0);
            assertEquals(1.0, context.delta());
            return null;
        }).when(flows).execute(any(AIFlowContext.class));
        engine.update(1.0);
        engine.update(0);
        verify(flows).execute(any(AIFlowContext.class));
    }

    @Test
    void rejectsInvalidIntervals() {
        for (double interval : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> engine(interval));
        }
    }
}
