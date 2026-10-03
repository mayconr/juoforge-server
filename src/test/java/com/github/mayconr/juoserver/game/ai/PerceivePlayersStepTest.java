package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.PerceivePlayersStep;
import com.github.mayconr.juoserver.game.model.BehaviorDefinition;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.World;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerceivePlayersStepTest {
    @Test
    void filtersCandidatesAndReplacesPreviousPerceptionWithoutSelectingTarget() {
        var world = mock(World.class, RETURNS_DEEP_STUBS);
        var npc = mock(UONpc.class);
        when(npc.getBehavior()).thenReturn(new BehaviorDefinition("COMBAT", 24, 3, null, List.of()));
        var alive = player(true, true);
        var dead = player(true, false);
        var offline = player(false, true);
        var anotherNpc = mock(UONpc.class);
        List<UOMobile> candidates = List.of(alive, dead, offline, anotherNpc);
        when(world.storage().getMobilesInRange(eq(npc), eq(3), any())).thenAnswer(invocation -> {
            Predicate<UOMobile> filter = invocation.getArgument(2);
            return candidates.stream().filter(filter).toList();
        });
        var context = new CombatAIContext(npc, world);
        context.setNearbyPlayers(List.of(offline));
        context.setTarget(offline);
        var step = new PerceivePlayersStep();
        assertTrue(step.execute(context).shouldContinue());
        assertEquals(List.of(alive), context.getNearbyPlayers());
        assertSame(offline, context.getTarget());
        assertTrue(context.actions().isEmpty());
        when(world.storage().getMobilesInRange(eq(npc), eq(3), any())).thenReturn(List.of());
        assertTrue(step.execute(context).shouldContinue());
        assertTrue(context.getNearbyPlayers().isEmpty());
    }

    @Test
    void missingRadiusClearsStalePerceptionAndStopsFlow() {
        var npc = mock(UONpc.class);
        var world = mock(World.class);
        when(npc.getBehavior()).thenReturn(new BehaviorDefinition("COMBAT", 24, null, null, List.of()));
        var context = new CombatAIContext(npc, world);
        context.setNearbyPlayers(List.of(mock(UOPlayer.class)));
        var result = new PerceivePlayersStep().execute(context);
        assertEquals("AI_PERCEPTION_RADIUS_MISSING", result.code());
        assertTrue(result.shouldStop());
        assertTrue(context.getNearbyPlayers().isEmpty());
        verifyNoInteractions(world);
    }

    private UOPlayer player(boolean connected, boolean alive) {
        var player = mock(UOPlayer.class);
        when(player.isConnected()).thenReturn(connected);
        when(player.isAlive()).thenReturn(alive);
        return player;
    }
}
