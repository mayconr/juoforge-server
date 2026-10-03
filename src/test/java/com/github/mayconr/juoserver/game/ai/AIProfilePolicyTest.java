package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.vendor.VendorAIContext;
import com.github.mayconr.juoserver.game.ai.policy.AISpeechPolicy;
import com.github.mayconr.juoserver.game.ai.policy.NearbyPlayerActivationPolicy;
import com.github.mayconr.juoserver.game.model.BehaviorDefinition;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.world.World;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIProfilePolicyTest {
    @Test
    void activationUsesProfileRadiusWithGlobalFallback() {
        var world = mock(World.class, RETURNS_DEEP_STUBS);
        var npc = mock(UONpc.class);
        var player = mock(UOPlayer.class);
        var context = new AIFlowContext(npc, world);
        when(npc.getBehavior()).thenReturn(new BehaviorDefinition("AGGRESSIVE", 8, 3, null, List.of()));
        when(world.storage().getMobilesInRange(eq(npc), eq(8), any())).thenReturn(List.of(player));
        assertTrue(new NearbyPlayerActivationPolicy(24).isActive(context));
        verify(world.storage()).getMobilesInRange(eq(npc), eq(8), any());
        when(npc.getBehavior()).thenReturn(new BehaviorDefinition("AGGRESSIVE", null, 3, null, List.of()));
        new NearbyPlayerActivationPolicy(24).isActive(context);
        verify(world.storage()).getMobilesInRange(eq(npc), eq(24), any());
    }

    @Test
    void speechUsesConfiguredRadiusAndNormalizedTriggersOnlyForDialogue() {
        var npc = mock(UONpc.class);
        var player = mock(UOPlayer.class);
        var world = mock(World.class);
        when(player.isConnected()).thenReturn(true);
        when(npc.getBehavior()).thenReturn(new BehaviorDefinition("DIALOGUE_REACTIVE", 24,
                null, 3, List.of("buy")));
        var context = new VendorAIContext(npc, world, "ORE");
        var policy = AISpeechPolicy.nearbyVendor(24);
        assertTrue(policy.accepts(context, new MobileSpeech(player, " BUY ", null)));
        assertFalse(policy.accepts(context, new MobileSpeech(player, "hello", null)));
        when(player.getX()).thenReturn(4);
        assertFalse(policy.accepts(context, new MobileSpeech(player, "buy", null)));
        when(player.getX()).thenReturn(0);
        assertFalse(policy.accepts(new AggressiveAIContext(npc, world), new MobileSpeech(player, "buy", null)));
        when(player.isConnected()).thenReturn(false);
        assertFalse(policy.accepts(context, new MobileSpeech(player, "buy", null)));
    }
}
