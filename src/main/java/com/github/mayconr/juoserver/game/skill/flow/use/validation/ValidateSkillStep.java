package com.github.mayconr.juoserver.game.skill.flow.use.validation;

import com.github.mayconr.juoserver.game.skill.flow.use.UseSkillContext;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateSkillStep extends AbstractFlowStep<UseSkillContext> {

    private final UOFileReader fileReader;

    public ValidateSkillStep(UOFileReader fileReader) {
        super("validateSkill");
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(UseSkillContext context) {
        if (context.getPlayer() == null) {
            return StepResult.failure("Player is null");
        }

        if (context.getSkillId() < 0) {
            return StepResult.failure("Invalid Skill ID");
        }

        var skillOpt = fileReader.getSkill(context.getSkillId());
        if (skillOpt.isEmpty()) {
            return StepResult.failure("Skill not found");
        }

        var skill = skillOpt.get();
        if (!skill.hasAction()) {
            return StepResult.failure("Skill does not have action");
        }

        return StepResult.success();
    }
}
