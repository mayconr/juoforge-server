package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.ai.policy.AIExecutionPolicy;
import com.github.mayconr.juoserver.game.ai.policy.AISpeechPolicy;
import com.github.mayconr.juoserver.game.ai.policy.NearbyPlayerExecutionPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIWorldConfiguratorTest {
    @Test
    void defaultPoliciesUseConfiguredVisibilityWhenCreated() {
        var cfg = new DefaultWorldConfigurator();
        var activationFactory = cfg.aiExecutionPolicy();
        var speechFactory = cfg.aiSpeechPolicy();
        cfg.gameplaySettings(cfg.gameplaySettings().withWorld(
                new com.github.mayconr.juoserver.game.GamePlaySettings.World(
                        new com.github.mayconr.juoserver.game.GamePlaySettings.Visibility(18), null)));

        assertEquals(new NearbyPlayerExecutionPolicy(18), activationFactory.get());
        assertNotNull(speechFactory.get());
    }

    @Test
    void shardCanOverridePoliciesWithoutContent() {
        WorldConfigurator cfg = new DefaultWorldConfigurator();
        AIExecutionPolicy activation = context -> true;
        AISpeechPolicy speech = (context, event) -> true;
        cfg.aiExecutionPolicy(() -> activation);
        cfg.aiSpeechPolicy(() -> speech);

        assertSame(activation, cfg.aiExecutionPolicy().get());
        assertSame(speech, cfg.aiSpeechPolicy().get());
        assertThrows(NullPointerException.class, () -> cfg.aiExecutionPolicy(null));
        assertThrows(NullPointerException.class, () -> cfg.aiSpeechPolicy(null));
    }
}
