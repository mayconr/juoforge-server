package com.github.mayconr.juoserver.game.skill.flow.stop;

import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Marks the player's active action to end on the next update; the update flow performs the cancellation. */
public final class RequestSkillActionStopStep extends AbstractFlowStep<StopSkillActionContext> {
    private final SkillModule skills;

    public RequestSkillActionStopStep(SkillModule skills) {
        super("request-skill-action-stop");
        this.skills = skills;
    }

    @Override
    public StepResult execute(StopSkillActionContext context) {
        var action = skills.getSkillActions().get(context.getPlayer().getSerialId());
        if (action != null) {
            action.requestStop();
        }
        context.complete(action != null);
        return StepResult.success();
    }
}
