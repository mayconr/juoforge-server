package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.SkillValue;
import lombok.RequiredArgsConstructor;

/** Pure gain formulas, independent of flows, randomness and event delivery. */
@RequiredArgsConstructor
public final class SkillGainCalculator {
    private final GamePlaySettings.Skills settings;

    public double calculateChance(SkillValue skill, double difficulty) {
        final var minGainChance = settings.minGainChance();
        final var maxGainChance = settings.maxGainChance();
        final var balanceOffset = settings.balanceOffset();

        final var rawChance =
                (difficulty - skill.getBase() + balanceOffset) / 100.0;

        return Math.clamp(rawChance, minGainChance, maxGainChance);
    }

    /**
     * Gain amount with monotonic difficulty scaling.
     * Higher difficulty will never reduce the gain.
     */
    public double calculateGainAmount(SkillValue skill, double difficulty) {
        final double baseGain = settings.baseGain();

        final double current = skill.getBase();
        final double cap = skill.getCap();

        if (cap <= 0) {
            return 0;
        }

        // Cap-based diminishing returns (smooth)
        double capFactor = 1.0 - (current / cap);
        capFactor = Math.clamp(capFactor, 0.1, 1.0);

        // Difficulty scaling (monotonic)
        double difficultyFactor = difficulty / cap;
        difficultyFactor = Math.clamp(difficultyFactor, 0.5, 1.5);

        // Multipliers <= 1 or a non-positive threshold disable the beginner bonus.
        final double beginnerGainMultiplier = settings.beginnerGainMultiplier();
        final double beginnerGainThreshold = settings.beginnerGainThreshold();
        double beginnerFactor = 1.0;
        if (beginnerGainMultiplier > 1.0 && beginnerGainThreshold > 0.0) {
            beginnerFactor += (beginnerGainMultiplier - 1.0)
                    * Math.clamp(1.0 - current / beginnerGainThreshold, 0.0, 1.0);
        }

        return baseGain * capFactor * difficultyFactor * beginnerFactor;
    }

}
