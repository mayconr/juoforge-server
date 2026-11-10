package com.github.mayconr.juoserver.game.skill.flow.update;

import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class AdvanceSkillActionsStep extends AbstractFlowStep<UpdateSkillActionsContext> {
    private final SkillModule skills;

    public AdvanceSkillActionsStep(SkillModule skills) {
        super("advance-skill-actions");
        this.skills = skills;
    }

    @Override
    public StepResult execute(UpdateSkillActionsContext context) {
        // Only actions in this snapshot can run during the current update.
        for (var entry : skills.getSkillActions().entrySet()) {
            if (entry.getValue().advance(context.getDelta())) {
                context.getReadyActions().add(entry);
            }
        }
        return StepResult.success();
    }
}
