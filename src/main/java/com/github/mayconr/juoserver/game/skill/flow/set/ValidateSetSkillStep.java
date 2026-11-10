package com.github.mayconr.juoserver.game.skill.flow.set;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ValidateSetSkillStep extends AbstractFlowStep<SetSkillContext> {
    public ValidateSetSkillStep() {
        super("validate-set-skill");
    }

    @Override
    public StepResult execute(SetSkillContext context) {
        if (context.getSkillId() < 0 || context.getSkillId() > 0xFFFF) {
            return StepResult.failure("INVALID_SKILL_ID", "Skill ID must fit an unsigned short");
        }
        if (!Double.isFinite(context.getValue())) {
            return StepResult.failure("INVALID_SKILL_VALUE", "Skill value must be finite");
        }
        return StepResult.success();
    }
}
