package com.github.mayconr.juoserver.game.skill.flow.start;

import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Captures a copy of the player position, so later movement can be detected against it. */
public final class CaptureSkillActionOriginStep extends AbstractFlowStep<StartSkillActionContext> {

    public CaptureSkillActionOriginStep() {
        super("capture-skill-action-origin");
    }

    @Override
    public StepResult execute(StartSkillActionContext context) {
        context.setOrigin(new PointInTheWorld(context.getPlayer()));
        return StepResult.success();
    }
}
