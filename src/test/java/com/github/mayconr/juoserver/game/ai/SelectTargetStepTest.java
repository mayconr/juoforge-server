package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.actions.CancelAttackAction;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIState;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.IdleStep;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.SelectTargetStep;
import com.github.mayconr.juoserver.game.ai.policy.TargetSelectionPolicy;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.World;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectTargetStepTest {
    private final CombatAIContext context = new CombatAIContext(mock(UONpc.class), mock(World.class));
    private final SelectTargetStep step = new SelectTargetStep();

    @Test
    void selectsNearestWithStableSerialTieBreakAndRejectsInvalidPlayers() {
        var farther = player(1, 3, 0);
        var tiedHigher = player(9, 1, 1);
        var tiedLower = player(4, 0, 1);
        var dead = player(2, 0, 0);
        var offline = player(3, 0, 0);
        when(dead.isAlive()).thenReturn(false);
        when(offline.isConnected()).thenReturn(false);
        context.setNearbyPlayers(List.of(farther, tiedHigher, dead, offline, tiedLower));
        assertTrue(step.execute(context).shouldContinue());
        assertSame(tiedLower, context.getTarget());
        assertEquals(CombatAIState.PURSUING, context.getState());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void retainsCurrentTargetEvenWhenAnotherPlayerIsCloser() {
        var current = player(10, 3, 0);
        context.setTarget(current);
        context.setState(CombatAIState.ATTACKING);
        context.setNearbyPlayers(List.of(player(2, 1, 0), current));
        step.execute(context);
        assertSame(current, context.getTarget());
        assertEquals(CombatAIState.ATTACKING, context.getState());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void replacesLostOrDisconnectedTargetAndCancelsOnce() {
        var current = player(10, 3, 0);
        var replacement = player(11, 1, 0);
        context.setTarget(current);
        context.setState(CombatAIState.ATTACKING);
        context.setNearbyPlayers(List.of(current, replacement));
        when(current.isConnected()).thenReturn(false);
        step.execute(context);
        assertSame(replacement, context.getTarget());
        assertEquals(CombatAIState.PURSUING, context.getState());
        var cancellation = assertInstanceOf(CancelAttackAction.class, context.actions().poll());
        assertSame(context.npc(), cancellation.npc());
        step.execute(context);
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void lossOfPerceptionLeavesCancellationToIdle() {
        context.setTarget(player(1, 1, 0));
        context.setState(CombatAIState.ATTACKING);
        step.execute(context);
        assertNull(context.getTarget());
        assertEquals(CombatAIState.ATTACKING, context.getState());
        assertTrue(context.actions().isEmpty());
        new IdleStep().execute(context);
        assertEquals(CombatAIState.IDLE, context.getState());
        assertInstanceOf(CancelAttackAction.class, context.actions().poll());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void survivalDoesNotSelectOrReplaceTarget() {
        var current = player(1, 1, 0);
        context.setTarget(current);
        context.setNearbyPlayers(List.of(player(2, 0, 0)));
        for (var state : List.of(CombatAIState.FLEEING, CombatAIState.RECOVERING)) {
            context.setState(state);
            step.execute(context);
            assertSame(current, context.getTarget());
            assertEquals(state, context.getState());
            assertTrue(context.actions().isEmpty());
        }
    }

    @Test
    void eligibilityCanBeReplacedForShardRules() {
        var rejected = player(1, 0, 0);
        var accepted = player(2, 2, 0);
        context.setNearbyPlayers(List.of(rejected, accepted));
        new SelectTargetStep((ctx, player) -> player.getSerialId() == 2,
                TargetSelectionPolicy.nearestPlayer()).execute(context);
        assertSame(accepted, context.getTarget());
    }

    private UOPlayer player(int serial, int x, int y) {
        var player = mock(UOPlayer.class);
        when(player.getSerialId()).thenReturn(serial);
        when(player.getX()).thenReturn(x);
        when(player.getY()).thenReturn(y);
        when(player.isConnected()).thenReturn(true);
        when(player.isAlive()).thenReturn(true);
        return player;
    }
}
