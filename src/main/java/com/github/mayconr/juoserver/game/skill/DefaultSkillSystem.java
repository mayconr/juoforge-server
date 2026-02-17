package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.SkillGained;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSkillSystem implements SkillSystem {

    private final ServerProperties properties;
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
        final var minGainChance = properties.skills().minGainChance();
        final var maxGainChance = properties.skills().maxGainChance();
        final var balanceOffset = properties.skills().balanceOffset();

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

        return baseGain * capFactor * difficultyFactor;
    }

    private void applyGain(UOMobile mobile, SkillValue skill, double amount) {
        double currentBase = skill.getBase();

        skill.increase(amount);

        if (skill.getBase() - 0.1 > currentBase) {
            eventBus.publish(new SkillGained(mobile, skill));
        }
    }
}
