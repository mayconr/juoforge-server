package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.GamePlaySettings.Temporal;
import com.github.mayconr.juoserver.game.temporal.GameCalendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemporalSettingsTest {

    @Test
    void defaultsToFiveRealSecondsPerGameMinute() {
        assertEquals(5.0, new GamePlaySettings().temporal().realSecondsPerGameMinute());
    }

    @Test
    void preservesOverrideWhenSettingsAreCombined() {
        var rules = new GamePlaySettings().withTemporal(new Temporal(2.5));
        var updated = rules.withName("test")
                .withSkills(rules.skills())
                .withMobile(rules.mobile())
                .withWorld(rules.world())
                .withClient(rules.client())
                .withEconomy(rules.economy())
                .withUi(rules.ui());

        var cfg = new DefaultWorldConfigurator();
        cfg.gameplaySettings(updated);
        cfg.serverSettings(new ServerSettings("/uo/data"));

        assertEquals(2.5, cfg.settings().temporal().realSecondsPerGameMinute());
    }

    @Test
    void convertsRealSecondsIntoGameSeconds() {
        assertEquals(60.0, GameCalendar.toGameSeconds(5, new Temporal()));
        assertEquals(120.0, GameCalendar.toGameSeconds(1, new Temporal(0.5)));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY})
    void rejectsInvalidConversion(double realSecondsPerGameMinute) {
        assertThrows(IllegalArgumentException.class, () -> new Temporal(realSecondsPerGameMinute));
    }
}
