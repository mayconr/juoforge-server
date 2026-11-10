package com.github.mayconr.juoserver.game.skill.flow.update;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ValidateSkillActionsUpdateStep extends AbstractFlowStep<UpdateSkillActionsContext> {
    public ValidateSkillActionsUpdateStep() {
        super("validate-skill-actions-update");
    }

    @Override
    public StepResult execute(UpdateSkillActionsContext context) {
        if (!Double.isFinite(context.getDelta()) || context.getDelta() < 0) {
            return StepResult.failure("INVALID_SKILL_ACTION_DELTA", "delta must be finite and non-negative");
        }
        return StepResult.success();
    }
}
