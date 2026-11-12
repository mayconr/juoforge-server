package com.github.mayconr.juoserver.game.skill.flow.update;

import com.github.mayconr.juoserver.game.interaction.WorldInteraction;
import com.github.mayconr.juoserver.game.skill.SkillAction;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;

/**
 * Starts the next swing cycle of every action that swung in this update and still has swings left,
 * sending its animation so each swing lands after its own animation. Released actions are skipped.
 */
@Slf4j
public final class AnimateSkillActionsStep extends AbstractFlowStep<UpdateSkillActionsContext> {
    private final SkillModule skills;
    private final WorldInteraction interaction;

    public AnimateSkillActionsStep(SkillModule skills, WorldInteraction interaction) {
        super("animate-skill-actions");
        this.skills = skills;
        this.interaction = interaction;
    }

    @Override
    public StepResult execute(UpdateSkillActionsContext context) {
        var reserved = skills.getSkillActions();
        for (var entry : context.getReadyActions()) {
            if (startsNextCycle(reserved, entry)) {
                animate(entry.getKey(), entry.getValue());
            }
        }
        return StepResult.success();
    }

    private static boolean startsNextCycle(Map<Integer, SkillAction> reserved, Entry<Integer, SkillAction> entry) {
        var action = entry.getValue();
        return action.getAnimation() != null
                && !action.isComplete()
                && reserved.get(entry.getKey()) == action;
    }

    private void animate(int playerSerial, SkillAction action) {
        try {
            var actor = action.getActor();
            interaction.sendAnimation(actor, action.getAnimation().optionsFor(actor));
        } catch (Exception exception) {
            log.error("Skill action animation failed for player {}", playerSerial, exception);
        }
    }
}
