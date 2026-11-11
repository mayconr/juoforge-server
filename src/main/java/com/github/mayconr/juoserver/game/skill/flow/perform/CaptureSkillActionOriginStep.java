package com.github.mayconr.juoserver.game.skill.flow.perform;

import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Captures a copy of the player position, so later movement can be detected against it. */
public final class CaptureSkillActionOriginStep extends AbstractFlowStep<PerformSkillContext> {

    public CaptureSkillActionOriginStep() {
        super("capture-skill-action-origin");
    }

    @Override
    public StepResult execute(PerformSkillContext context) {
        context.setOrigin(new PointInTheWorld(context.getPlayer()));
        return StepResult.success();
    }
}
