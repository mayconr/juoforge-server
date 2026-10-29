package com.github.mayconr.juoserver.game.skill.flow.perform;

import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ValidatePerformSkillStep extends AbstractFlowStep<PerformSkillContext> {
    private final UOFileReader fileReader;

    public ValidatePerformSkillStep(UOFileReader fileReader) {
        super("validate-perform-skill");
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(PerformSkillContext context) {
        var options = context.getOptions();
        if (context.getPlayer() == null || options == null || options.swing() == null
                || options.onComplete() == null || options.swings() <= 0
                || !Double.isFinite(options.delaySeconds()) || options.delaySeconds() < 0) {
            return StepResult.failure("INVALID_SKILL_ACTION", "Invalid player or skill action options");
        }
        if (options.skillId() == null || options.skillId() < 0 || options.skillId() > 0xFFFF) {
            return StepResult.failure("INVALID_SKILL_ID", "A valid skill ID is required");
        }
        var skill = fileReader.getSkill(options.skillId()).orElse(null);
        if (skill == null) {
            return StepResult.failure("SKILL_NOT_FOUND", "Skill not found: " + options.skillId());
        }
        return StepResult.success();
    }
}
