package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.CombatAIState;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.PursueTargetStep;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.World;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PursueTargetStepTest {
    private final UONpc npc = mock(UONpc.class);
    private final UOPlayer target = mock(UOPlayer.class);
    private final World world = mock(World.class, RETURNS_DEEP_STUBS);
    private final AggressiveAIContext context = new AggressiveAIContext(npc, world);
    private final PursueTargetStep step = new PursueTargetStep();

    private void targetAt(int x, int y) {
        when(target.isConnected()).thenReturn(true);
        when(target.isAlive()).thenReturn(true);
        when(target.getX()).thenReturn(x);
        when(target.getY()).thenReturn(y);
        context.setTarget(target);
    }

    @Test
    void emitsOneMovePerAiTickAndTracksCurrentTargetPosition() {
        targetAt(5, -4);
        assertTrue(step.execute(context).shouldStop());
        assertEquals(CombatAIState.PURSUING, context.getState());
        assertEquals(new WalkAction(npc, Direction.NORTHEAST), context.actions().poll());
        assertTrue(context.actions().isEmpty());
        targetAt(-5, 0);
        step.execute(context);
        assertEquals(new WalkAction(npc, Direction.WEST), context.actions().poll());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void withinMeleeRangeAllowsAttackWithoutMovement() {
        targetAt(1, 1);
        assertTrue(step.execute(context).shouldContinue());
        assertEquals(CombatAIState.ATTACKING, context.getState());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void respectsEquippedWeaponRange() {
        targetAt(5, 0);
        when(npc.getEquippedItems()).thenReturn(Map.of(Layer.TWO_HANDED, 100));
        var weapon = mock(UOItem.class, RETURNS_DEEP_STUBS);
        when(weapon.getTemplate().weapon().radius()).thenReturn(6);
        when(world.mobile().getEquippedItems(npc)).thenReturn(Map.of(Layer.TWO_HANDED, weapon));
        assertTrue(step.execute(context).shouldContinue());
        assertEquals(CombatAIState.ATTACKING, context.getState());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void absentOrInvalidTargetDoesNotMove() {
        assertTrue(step.execute(context).shouldContinue());
        targetAt(5, 0);
        when(target.isConnected()).thenReturn(false);
        step.execute(context);
        assertNull(context.getTarget());
        targetAt(5, 0);
        when(target.isAlive()).thenReturn(false);
        step.execute(context);
        assertNull(context.getTarget());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void survivalAndDifferentElevationDoNotProducePursuitMovement() {
        targetAt(5, 0);
        for (var state : List.of(CombatAIState.FLEEING, CombatAIState.RECOVERING)) {
            context.setState(state);
            step.execute(context);
            assertEquals(state, context.getState());
            assertTrue(context.actions().isEmpty());
        }
        context.setState(CombatAIState.PURSUING);
        targetAt(0, 0);
        when(target.getZ()).thenReturn(9);
        assertTrue(step.execute(context).shouldStop());
        assertEquals(CombatAIState.PURSUING, context.getState());
        assertTrue(context.actions().isEmpty());
    }
}
