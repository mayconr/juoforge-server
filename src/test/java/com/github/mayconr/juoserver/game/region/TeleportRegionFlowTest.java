package com.github.mayconr.juoserver.game.region;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.region.flow.transition.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.region.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeleportRegionFlowTest {
    @ParameterizedTest
    @ValueSource(strings = {"teleport", "same", "missing-old", "missing-new", "normal"})
    void teleportsOnlyWhenEnteringTeleportRegionFromAnotherRegion(String scenario) {
        var regions = mock(RegionSystem.class);
        var events = mock(EventBus.class);
        var module = mock(RegionModule.class);
        var ai = mock(AIModule.class);
        var mobiles = mock(WorldMobile.class);
        var player = mock(UOPlayer.class);
        var from = new PointInTheWorld(1, 1, 0);
        var to = new PointInTheWorld(2, 2, 0);
        var oldRegion = mock(RegionNode.class);
        var newRegion = mock(RegionNode.class);
        when(newRegion.getType()).thenReturn(scenario.equals("normal") ? null : RegionType.TELEPORT);
        when(newRegion.getProperties()).thenReturn(Map.of(
                TeleportRegionStep.TELEPORT_DESTINATION_X, 100,
                TeleportRegionStep.TELEPORT_DESTINATION_Y, 200,
                TeleportRegionStep.TELEPORT_DESTINATION_Z, 5));
        when(regions.getRegion(from)).thenReturn(scenario.equals("missing-old") ? Optional.empty()
                : Optional.of(scenario.equals("same") ? newRegion : oldRegion));
        when(regions.getRegion(to)).thenReturn(scenario.equals("missing-new") ? Optional.empty() : Optional.of(newRegion));

        var result = RegionTransitionFlowDefinition.build(regions, events, module, ai, mobiles)
                .execute(new RegionTransitionContext(player, from, to));

        assertTrue(result.flowSucceeded());
        if (scenario.equals("teleport")) {
            var ordered = inOrder(module, mobiles);
            ordered.verify(module).updatePlayerRegion(player, newRegion);
            ordered.verify(mobiles).teleport(player, new PointInTheWorld(100, 200, 5));
        } else {
            verifyNoInteractions(mobiles);
        }
    }
}
