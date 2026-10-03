package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.actions.CancelAttackAction;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.CombatAIState;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.IdleStep;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.World;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdleStepTest {
    private final AggressiveAIContext context = new AggressiveAIContext(mock(UONpc.class), mock(World.class));
    private final IdleStep step = new IdleStep();

    @Test
    void idleStandsStillAndEndsCycle() {
        assertTrue(step.execute(context).shouldStop());
        assertEquals(CombatAIState.IDLE, context.getState());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void leavingCombatWithoutTargetCancelsOnce() {
        for (var state : new CombatAIState[]{CombatAIState.ATTACKING, CombatAIState.PURSUING}) {
            context.setState(state);
            assertTrue(step.execute(context).shouldStop());
            assertEquals(CombatAIState.IDLE, context.getState());
            var cancellation = assertInstanceOf(CancelAttackAction.class, context.actions().poll());
            assertSame(context.npc(), cancellation.npc());
            assertTrue(step.execute(context).shouldStop());
            assertTrue(context.actions().isEmpty());
        }
    }

    @Test
    void preservesSurvivalAndStatesWithTarget() {
        for (var state : new CombatAIState[]{CombatAIState.FLEEING, CombatAIState.RECOVERING}) {
            context.setState(state);
            assertTrue(step.execute(context).shouldContinue());
            assertEquals(state, context.getState());
            assertTrue(context.actions().isEmpty());
        }
        var target = mock(UOPlayer.class);
        context.setTarget(target);
        context.setState(CombatAIState.ATTACKING);
        assertTrue(step.execute(context).shouldContinue());
        assertSame(target, context.getTarget());
        assertEquals(CombatAIState.ATTACKING, context.getState());
        assertTrue(context.actions().isEmpty());
    }
}
