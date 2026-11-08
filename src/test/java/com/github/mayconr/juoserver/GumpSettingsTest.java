package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GumpSettingsTest {
    @Test
    void defaultsToThirtySecondsAndPreservesOverridesWhenSettingsAreCombined() {
        assertEquals(30, new GamePlaySettings().ui().defaultGumpTimeoutSeconds());
        var rules = new GamePlaySettings().withUi(new GamePlaySettings.Ui(45));
        var updated = rules.withName("test").withSkills(rules.skills()).withMobile(rules.mobile())
                .withWorld(rules.world()).withClient(rules.client()).withEconomy(rules.economy());
        var cfg = new DefaultWorldConfigurator();
        cfg.gameplaySettings(updated);
        cfg.serverSettings(new ServerSettings("/uo/data"));
        assertEquals(45, cfg.settings().ui().defaultGumpTimeoutSeconds());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE})
    void rejectsInvalidTimeouts(double seconds) {
        assertThrows(IllegalArgumentException.class, () -> new GamePlaySettings.Ui(seconds));
    }
}
