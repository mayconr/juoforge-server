package com.github.mayconr.juoserver.game.skill.flow.gain;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ResolveGainSkillStep extends AbstractFlowStep<TryGainSkillContext> {
    public ResolveGainSkillStep() {
        super("resolve-gain-skill");
    }

    @Override
    public StepResult execute(TryGainSkillContext context) {
        var skill = context.getMobile().getSkills().get(context.getSkillId());
        if (skill == null) {
            return StepResult.failure("SKILL_NOT_FOUND", "Mobile has no skill " + context.getSkillId());
        }
        context.setSkill(skill);
        return StepResult.success();
    }
}
