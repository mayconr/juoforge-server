package com.github.mayconr.juoserver.game.skill.flow.gain;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ApplySkillGainStep extends AbstractFlowStep<TryGainSkillContext> {
    public ApplySkillGainStep() {
        super("apply-skill-gain");
    }

    @Override
    public StepResult execute(TryGainSkillContext context) {
        var skill = context.getSkill();
        int previousClientBase = (int) (skill.getBase() * 10);
        skill.increase(context.getGainAmount());
        // Keep fractional gains; the client displays skill points in tenths.
        context.setClientBaseIncreased((int) (skill.getBase() * 10) > previousClientBase);
        return StepResult.success();
    }
}
