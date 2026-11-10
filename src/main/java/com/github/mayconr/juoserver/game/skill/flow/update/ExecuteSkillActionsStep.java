package com.github.mayconr.juoserver.game.skill.flow.update;

import com.github.mayconr.juoserver.game.skill.SkillAction;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ExecuteSkillActionsStep extends AbstractFlowStep<UpdateSkillActionsContext> {
    private final SkillModule skills;

    public ExecuteSkillActionsStep(SkillModule skills) {
        super("execute-skill-actions");
        this.skills = skills;
    }

    @Override
    public StepResult execute(UpdateSkillActionsContext context) {
        for (var entry : context.getReadyActions()) {
            executeAction(entry.getKey(), entry.getValue());
        }
        return StepResult.success();
    }

    private void executeAction(int playerSerial, SkillAction action) {
        try {
            action.executeSwing();
            if (action.isComplete()) {
                skills.releaseSkillAction(playerSerial, action);
                action.complete();
            }
        } catch (Exception exception) {
            skills.releaseSkillAction(playerSerial, action);
            log.error("Skill action failed for player {}", playerSerial, exception);
        }
    }
}
