package com.github.mayconr.juoserver.game.skill.flow.gain;

import com.github.mayconr.juoserver.game.model.event.SkillGained;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class NotifySkillGainStep extends AbstractFlowStep<TryGainSkillContext> {
    private final EventBus events;

    public NotifySkillGainStep(EventBus events) {
        super("notify-skill-gain");
        this.events = events;
    }

    @Override
    public StepResult execute(TryGainSkillContext context) {
        events.publish(new SkillGained(context.getMobile(), context.getSkill()));
        return StepResult.success();
    }
}
