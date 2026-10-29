package com.github.mayconr.juoserver.game.skill.flow.gain;

import com.github.mayconr.juoserver.game.skill.SkillGainCalculator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;

public final class RollSkillGainStep extends AbstractFlowStep<TryGainSkillContext> {
    private final SkillGainCalculator calculator;
    private final RNG rng;

    public RollSkillGainStep(SkillGainCalculator calculator, RNG rng) {
        super("roll-skill-gain");
        this.calculator = calculator;
        this.rng = rng;
    }

    @Override
    public StepResult execute(TryGainSkillContext context) {
        double chance = calculator.calculateChance(context.getSkill(), context.getDifficulty());
        return rng.roll(chance) ? StepResult.success() : StepResult.stop();
    }
}
