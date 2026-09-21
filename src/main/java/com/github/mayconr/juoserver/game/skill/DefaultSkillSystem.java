package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.SkillGained;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSkillSystem implements SkillSystem {

    private final GamePlaySettings settings;
    private final RNG rng;
    private final EventBus eventBus;

    @Override
    public void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context) {
        final var skill = mobile.getSkills().get(skillId);

        final var chance = calculateChance(skill, difficulty);

        if (!rng.roll(chance)) {
            return;
        }

        final var amount = calculateGainAmount(skill, difficulty);

        applyGain(mobile, skill, amount);
    }

    public double calculateChance(SkillValue skill, double difficulty) {
        final var minGainChance = settings.skills().minGainChance();
        final var maxGainChance = settings.skills().maxGainChance();
        final var balanceOffset = settings.skills().balanceOffset();

        final var rawChance =
                (difficulty - skill.getBase() + balanceOffset) / 100.0;

        return Math.clamp(rawChance, minGainChance, maxGainChance);
    }

    /**
     * Gain amount with monotonic difficulty scaling.
     * Higher difficulty will never reduce the gain.
     */
    public double calculateGainAmount(SkillValue skill, double difficulty) {
        final double baseGain = 0.1;

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
        final double beginnerGainMultiplier = settings.skills().beginnerGainMultiplier();
        final double beginnerGainThreshold = settings.skills().beginnerGainThreshold();
        double beginnerFactor = 1.0;
        if (beginnerGainMultiplier > 1.0 && beginnerGainThreshold > 0.0) {
            beginnerFactor += (beginnerGainMultiplier - 1.0)
                    * Math.clamp(1.0 - current / beginnerGainThreshold, 0.0, 1.0);
        }

        return baseGain * capFactor * difficultyFactor * beginnerFactor;
    }

    private void applyGain(UOMobile mobile, SkillValue skill, double amount) {
        int previousClientBase = (int) (skill.getBase() * 10);

        skill.increase(amount);

        // Preserve fractional gains; notify only when the client-visible tenth increases.
        if ((int) (skill.getBase() * 10) > previousClientBase) {
            eventBus.publish(new SkillGained(mobile, skill));
        }
    }
}
