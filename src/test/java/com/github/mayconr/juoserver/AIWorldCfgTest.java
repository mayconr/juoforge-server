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
        cfg.gameplaySettings(cfg.gameplaySettings().withWorld(
                new com.github.mayconr.juoserver.game.GamePlaySettings.World(
                        new com.github.mayconr.juoserver.game.GamePlaySettings.Visibility(18), null)));

        assertEquals(new NearbyPlayerActivationPolicy(18), activationFactory.get());
        assertNotNull(speechFactory.get());
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
