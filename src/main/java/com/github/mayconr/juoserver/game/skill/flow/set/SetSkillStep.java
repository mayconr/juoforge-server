package com.github.mayconr.juoserver.game.skill.flow.set;

import com.github.mayconr.juoserver.game.model.event.SkillUpdated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class SetSkillStep extends AbstractFlowStep<SetSkillContext> {
    private final EventBus events;

    public SetSkillStep(EventBus events) {
        super("set-skill");
        this.events = events;
    }

    @Override
    public StepResult execute(SetSkillContext context) {
        var skill = context.getPlayer().getSkills().get(context.getSkillId());
        skill.setBase(context.getValue());
        events.publish(new SkillUpdated(context.getPlayer(), skill));
        return StepResult.success();
    }
}
