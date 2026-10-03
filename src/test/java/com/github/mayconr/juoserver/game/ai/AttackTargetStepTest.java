package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.actions.AttackAction;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIState;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.AttackTargetStep;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.World;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttackTargetStepTest {
    private final UONpc npc = mock(UONpc.class);
    private final UOPlayer target = mock(UOPlayer.class);
    private final CombatAIContext context = new CombatAIContext(npc, mock(World.class, RETURNS_DEEP_STUBS));
    private final AttackTargetStep step = new AttackTargetStep();

    private void prepareTarget() {
        when(npc.isAlive()).thenReturn(true);
        when(target.isAlive()).thenReturn(true);
        when(target.isConnected()).thenReturn(true);
        when(target.getSerialId()).thenReturn(42);
        when(target.getX()).thenReturn(1);
        context.setTarget(target);
    }

    @Test
    void requestsAttackAndEndsTheCycle() {
        prepareTarget();
        assertTrue(step.execute(context).shouldStop());
        assertEquals(CombatAIState.ATTACKING, context.getState());
        assertEquals(new AttackAction(npc, 42), context.actions().poll());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void outOfRangePreservesTargetAndReturnsToPursuit() {
        prepareTarget();
        when(target.getX()).thenReturn(5);
        assertTrue(step.execute(context).shouldStop());
        assertEquals(CombatAIState.PURSUING, context.getState());
        assertSame(target, context.getTarget());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void invalidTargetIsClearedAndDeadNpcDoesNotAttack() {
        prepareTarget();
        when(target.isConnected()).thenReturn(false);
        assertTrue(step.execute(context).shouldContinue());
        assertNull(context.getTarget());
        prepareTarget();
        when(target.isAlive()).thenReturn(false);
        step.execute(context);
        assertNull(context.getTarget());
        prepareTarget();
        when(npc.isAlive()).thenReturn(false);
        assertTrue(step.execute(context).shouldStop());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void noTargetOrSurvivalDoesNotAttack() {
        assertTrue(step.execute(context).shouldContinue());
        prepareTarget();
        for (var state : List.of(CombatAIState.FLEEING, CombatAIState.RECOVERING)) {
            context.setState(state);
            step.execute(context);
            assertEquals(state, context.getState());
            assertTrue(context.actions().isEmpty());
        }
    }
}
