package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.CombatAIState;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.CheckPursuitMovementStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.EnqueuePursuitMovementStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.EnqueuePursuitPathMovementStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.CalculatePursuitPathStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.PursueTargetStep;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PursueTargetStepTest {
    private final UONpc npc = mock(UONpc.class);
    private final UOPlayer target = mock(UOPlayer.class);
    private final World world = mock(World.class, RETURNS_DEEP_STUBS);
    private final AggressiveAIContext context = new AggressiveAIContext(npc, world);
    private final UOFileReader fileReader = mock(UOFileReader.class);
    private final Flow<AggressiveAIContext> pursuit = FlowFactory.<AggressiveAIContext>builder()
            .step(new PursueTargetStep())
            .step(new CheckPursuitMovementStep(fileReader))
            .step(new CalculatePursuitPathStep(fileReader), context ->
                    context.getPursuit().isMovementBlocked()
                            && !context.getPursuit().hasPathFor(context.npc(), context.getTarget()))
            .step(new EnqueuePursuitPathMovementStep(), context -> context.getPursuit().isMovementBlocked())
            .step(new EnqueuePursuitMovementStep())
            .build();

    private void targetAt(int x, int y) {
        when(target.isConnected()).thenReturn(true);
        when(target.isAlive()).thenReturn(true);
        when(target.getX()).thenReturn(x);
        when(target.getY()).thenReturn(y);
        context.setTarget(target);
    }

    @Test
    void emitsOneMovePerAiTickAndTracksCurrentTargetPosition() {
        when(fileReader.resolveWalkZ(npc, Direction.NORTHEAST)).thenReturn(OptionalInt.of(1));
        when(fileReader.resolveWalkZ(npc, Direction.WEST)).thenReturn(OptionalInt.of(0));
        targetAt(5, -4);
        assertTrue(pursuit.execute(context).shouldStop());
        assertEquals(CombatAIState.PURSUING, context.getState());
        assertEquals(new WalkAction(npc, Direction.NORTHEAST), context.actions().poll());
        verify(fileReader).hasBlockingCollision(npc, 1, -1, 1);
        assertTrue(context.actions().isEmpty());
        targetAt(-5, 0);
        pursuit.execute(context);
        assertEquals(new WalkAction(npc, Direction.WEST), context.actions().poll());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void detoursWhenTheDiagonalHasNoWalkableZAndReusesTheFirstStepAtTheOrigin() {
        when(npc.getX()).thenReturn(10);
        when(npc.getY()).thenReturn(20);
        when(npc.getZ()).thenReturn(7);
        targetAt(12, 19);
        when(target.getZ()).thenReturn(8);
        when(fileReader.resolveWalkZ(any(Location.class), any(Direction.class))).thenAnswer(invocation -> {
            Location from = invocation.getArgument(0);
            Direction direction = invocation.getArgument(1);
            if (from.getX() == 10 && direction == Direction.EAST) {
                return OptionalInt.of(7);
            }
            if (from.getX() == 11 && direction == Direction.NORTHEAST) {
                return OptionalInt.of(8);
            }
            return OptionalInt.empty();
        });

        assertTrue(pursuit.execute(context).shouldStop());

        verify(fileReader).resolveWalkZ(npc, Direction.NORTHEAST);
        verify(fileReader, never()).hasBlockingCollision(npc, 11, 19, 7);
        verify(fileReader).hasBlockingCollision(npc, 11, 20, 7);
        verify(fileReader).hasBlockingCollision(npc, 12, 19, 8);
        assertTrue(context.getPursuit().isMovementBlocked());
        var path = context.getPursuit().getPath();
        assertEquals(List.of(Direction.EAST, Direction.NORTHEAST), path.directions());
        assertEquals(new WalkAction(npc, Direction.EAST), context.actions().poll());
        assertTrue(context.actions().isEmpty());

        assertTrue(pursuit.execute(context).shouldStop());
        assertSame(path, context.getPursuit().getPath());
        assertEquals(new WalkAction(npc, Direction.EAST), context.actions().poll());
        assertTrue(context.actions().isEmpty());

        targetAt(15, 20);
        assertTrue(pursuit.execute(context).shouldStop());
        assertFalse(context.getPursuit().isMovementBlocked());
        assertEquals(new WalkAction(npc, Direction.EAST), context.actions().poll());
    }

    @Test
    void queuesNoMovementWhenTheDirectStepIsBlockedAndThePathIsEmpty() {
        targetAt(5, 0);
        when(fileReader.resolveWalkZ(npc, Direction.EAST)).thenReturn(OptionalInt.of(0));
        when(fileReader.hasBlockingCollision(npc, 1, 0, 0)).thenReturn(true);

        assertTrue(pursuit.execute(context).shouldStop());

        assertTrue(context.getPursuit().getPath().directions().isEmpty());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void withinMeleeRangeAllowsAttackWithoutMovement() {
        targetAt(1, 1);
        assertTrue(pursuit.execute(context).shouldContinue());
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
        assertTrue(pursuit.execute(context).shouldContinue());
        assertEquals(CombatAIState.ATTACKING, context.getState());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void absentOrInvalidTargetDoesNotMove() {
        assertTrue(pursuit.execute(context).shouldContinue());
        targetAt(5, 0);
        when(target.isConnected()).thenReturn(false);
        pursuit.execute(context);
        assertNull(context.getTarget());
        targetAt(5, 0);
        when(target.isAlive()).thenReturn(false);
        pursuit.execute(context);
        assertNull(context.getTarget());
        assertTrue(context.actions().isEmpty());
    }

    @Test
    void survivalAndDifferentElevationDoNotProducePursuitMovement() {
        targetAt(5, 0);
        for (var state : List.of(CombatAIState.FLEEING, CombatAIState.RECOVERING)) {
            context.setState(state);
            pursuit.execute(context);
            assertEquals(state, context.getState());
            assertTrue(context.actions().isEmpty());
        }
        context.setState(CombatAIState.PURSUING);
        targetAt(0, 0);
        when(target.getZ()).thenReturn(9);
        assertTrue(pursuit.execute(context).shouldStop());
        assertEquals(CombatAIState.PURSUING, context.getState());
        assertTrue(context.actions().isEmpty());
    }
}
