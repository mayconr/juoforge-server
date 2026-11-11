package com.github.mayconr.juoserver.game.skill.flow.update;

import com.github.mayconr.juoserver.game.skill.SkillAction;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;

/**
 * Cancels the remaining swings of reserved actions whose player moved since the action was requested.
 * Every update checks all reservations, not only ready ones, so leaving and returning between swings
 * is still detected. The action is released and completes once with the swings already executed.
 */
@Slf4j
public final class CancelMovedSkillActionsStep extends AbstractFlowStep<UpdateSkillActionsContext> {
    private final SkillModule skills;

    public CancelMovedSkillActionsStep(SkillModule skills) {
        super("cancel-moved-skill-actions");
        this.skills = skills;
    }

    @Override
    public StepResult execute(UpdateSkillActionsContext context) {
        for (var entry : skills.getSkillActions().entrySet()) {
            if (entry.getValue().hasMoved()) {
                cancel(entry.getKey(), entry.getValue());
                context.getReadyActions().removeIf(ready -> isSameAction(ready, entry.getValue()));
            }
        }
        return StepResult.success();
    }

    private static boolean isSameAction(Entry<Integer, SkillAction> ready, SkillAction action) {
        return ready.getValue() == action;
    }

    private void cancel(int playerSerial, SkillAction action) {
        skills.releaseSkillAction(playerSerial, action);
        try {
            action.complete();
        } catch (Exception exception) {
            log.error("Skill action completion failed after movement for player {}", playerSerial, exception);
        }
    }
}
