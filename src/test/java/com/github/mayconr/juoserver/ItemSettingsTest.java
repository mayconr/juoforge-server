package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.GamePlaySettings.ItemQuality;
import com.github.mayconr.juoserver.game.GamePlaySettings.ItemDecay;
import com.github.mayconr.juoserver.game.GamePlaySettings.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemSettingsTest {

    @Test
    void decayDefaultsToTwoMinutesWithOneMinuteScans() {
        assertEquals(new ItemDecay(120, 60), new GamePlaySettings().items().decay());
        var custom = new Items(null, new ItemDecay(300, 15));
        assertEquals(custom, new GamePlaySettings().withItems(custom).withName("test").items());
    }

    @ParameterizedTest
    @CsvSource({"0, 60", "-1, 60", "NaN, 60", "Infinity, 60", "120, 0", "120, -1", "120, NaN", "120, Infinity"})
    void decayRejectsInvalidDurations(double duration, double interval) {
        assertThrows(IllegalArgumentException.class, () -> new ItemDecay(duration, interval));
    }

    @Test
    void defaultsToModifierZeroToHundredAndSymmetricLuck() {
        assertEquals(new ItemQuality(0.0, 100.0, -100, 100), new GamePlaySettings().items().quality());
    }

    @Test
    void preservesOverrideWhenSettingsAreCombined() {
        var quality = new ItemQuality(10, 120, -50, 300);
        var rules = new GamePlaySettings().withItems(new Items(quality));
        var updated = rules.withName("test")
                .withSkills(rules.skills())
                .withMobile(rules.mobile())
                .withWorld(rules.world())
                .withClient(rules.client())
                .withEconomy(rules.economy())
                .withUi(rules.ui())
                .withTemporal(rules.temporal());

        var cfg = new DefaultWorldConfigurator();
        cfg.gameplaySettings(updated);
        cfg.serverSettings(new ServerSettings("/uo/data"));

        assertEquals(quality, cfg.settings().items().quality());
    }

    @Test
    void missingQualityUsesDefaults() {
        assertEquals(new ItemQuality(), new Items(null).quality());
    }

    @ParameterizedTest
    @CsvSource({"100, 100, -100, 100", "100, 0, -100, 100", "NaN, 100, -100, 100", "0, Infinity, -100, 100",
            "0, 100, 1, 100", "0, 100, -100, -1"})
    void rejectsInvalidRanges(double minModifier, double maxModifier, int minLuck, int maxLuck) {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemQuality(minModifier, maxModifier, minLuck, maxLuck));
    }
}
