package com.github.mayconr.juoserver.game.skill.flow.gain;

import com.github.mayconr.juoserver.game.skill.SkillGainCalculator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class CalculateSkillGainStep extends AbstractFlowStep<TryGainSkillContext> {
    private final SkillGainCalculator calculator;

    public CalculateSkillGainStep(SkillGainCalculator calculator) {
        super("calculate-skill-gain");
        this.calculator = calculator;
    }

    @Override
    public StepResult execute(TryGainSkillContext context) {
        context.setGainAmount(calculator.calculateGainAmount(context.getSkill(), context.getDifficulty()));
        return StepResult.success();
    }
}
