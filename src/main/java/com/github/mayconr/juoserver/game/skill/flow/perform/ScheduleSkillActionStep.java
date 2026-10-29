package com.github.mayconr.juoserver.game.skill.flow.perform;

import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ScheduleSkillActionStep extends AbstractFlowStep<PerformSkillContext> {
    private final SkillModule skills;

    public ScheduleSkillActionStep(SkillModule skills) {
        super("schedule-skill-action");
        this.skills = skills;
    }

    @Override
    public StepResult execute(PerformSkillContext context) {
        context.complete(skills.scheduleSkillAction(context.getPlayer(), context.getOptions()));
        return StepResult.success();
    }
}
