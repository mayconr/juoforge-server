package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.actions.SpeechAction;
import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.ai.definition.animal.PassiveAnimalAIContext;
import com.github.mayconr.juoserver.game.ai.definition.animal.PassiveAnimalAIDefinition;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.World;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PassiveAnimalAIFlowTest {

    @Test
    void wandersThenAlertsOnceAndFleesAsPlayerApproaches() {
        var npc = mock(UONpc.class);
        var player = mock(UOPlayer.class);
        var world = mock(World.class);
        var mobile = mock(WorldMobile.class);
        when(world.mobile()).thenReturn(mobile);
        when(player.isConnected()).thenReturn(true);
        when(player.isAlive()).thenReturn(true);
        when(mobile.getMobilesInRange(eq(npc), anyInt(), any())).thenAnswer(invocation ->
                GameMath.isInRange(npc, player, invocation.getArgument(1)) ? List.of(player) : List.of());

        var context = new PassiveAnimalAIContext(npc, world);
        var flow = PassiveAnimalAIDefinition.build();
        context.setDelta(2.0);

        when(player.getX()).thenReturn(12);
        flow.execute(context);
        assertInstanceOf(WalkAction.class, context.actions().poll());

        when(player.getX()).thenReturn(8);
        flow.execute(context);
        var alert = assertInstanceOf(SpeechAction.class, context.actions().poll());
        assertSame(player, alert.target());
        assertTrue(context.actions().isEmpty());

        flow.execute(context);
        assertTrue(context.actions().isEmpty());

        when(player.getX()).thenReturn(4);
        flow.execute(context);
        var flee = assertInstanceOf(WalkAction.class, context.actions().poll());
        assertEquals(Direction.WEST, flee.direction());
        assertTrue(context.actions().isEmpty());

        when(player.getX()).thenReturn(12);
        flow.execute(context);
        assertInstanceOf(WalkAction.class, context.actions().poll());

        when(player.getX()).thenReturn(8);
        flow.execute(context);
        assertInstanceOf(SpeechAction.class, context.actions().poll());
    }
}
