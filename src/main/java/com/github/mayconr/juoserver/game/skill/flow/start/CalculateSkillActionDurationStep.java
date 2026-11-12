package com.github.mayconr.juoserver.game.skill.flow.start;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/**
 * Captures the action duration once, using the player's dexterity when the action starts.
 * The base duration applies at the reference dexterity; lower dexterity lengthens it and higher
 * dexterity shortens it, without a lower or upper limit.
 */
public final class CalculateSkillActionDurationStep extends AbstractFlowStep<StartSkillActionContext> {
    private static final double BASE_DURATION_SECONDS = 1.0;
    private static final double REFERENCE_DEXTERITY = 100.0;
    private static final double DEXTERITY_SCALE = 100.0;

    public CalculateSkillActionDurationStep() {
        super("calculate-skill-action-duration");
    }

    @Override
    public StepResult execute(StartSkillActionContext context) {
        int dexterity = Math.max(0, context.getPlayer().getDexterity());
        context.setDurationSeconds(durationFor(dexterity));
        return StepResult.success();
    }

    private static double durationFor(int dexterity) {
        return BASE_DURATION_SECONDS * (DEXTERITY_SCALE + REFERENCE_DEXTERITY) / (DEXTERITY_SCALE + dexterity);
    }
}
