package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.ai.policy.AIActivationPolicy;
import com.github.mayconr.juoserver.game.ai.policy.AISpeechPolicy;
import com.github.mayconr.juoserver.game.ai.policy.NearbyPlayerActivationPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIWorldCfgTest {
    @Test
    void defaultPoliciesUseConfiguredVisibilityWhenCreated() {
        var cfg = new DefaultWorldCfg();
        var activationFactory = cfg.aiActivationPolicy();
        var speechFactory = cfg.aiSpeechPolicy();
        var content = mock(WorldContent.class, RETURNS_DEEP_STUBS);
        when(content.settings().world().visibility().range()).thenReturn(18);
        cfg.content(content);

        assertEquals(new NearbyPlayerActivationPolicy(18), activationFactory.get());
        assertNotNull(speechFactory.get());
        verify(content.settings().world().visibility(), times(2)).range();
    }

    @Test
    void shardCanOverridePoliciesWithoutContent() {
        WorldCfg cfg = new DefaultWorldCfg();
        AIActivationPolicy activation = context -> true;
        AISpeechPolicy speech = (context, event) -> true;
        cfg.aiActivationPolicy(() -> activation);
        cfg.aiSpeechPolicy(() -> speech);

        assertSame(activation, cfg.aiActivationPolicy().get());
        assertSame(speech, cfg.aiSpeechPolicy().get());
        assertThrows(NullPointerException.class, () -> cfg.aiActivationPolicy(null));
        assertThrows(NullPointerException.class, () -> cfg.aiSpeechPolicy(null));
    }
}
