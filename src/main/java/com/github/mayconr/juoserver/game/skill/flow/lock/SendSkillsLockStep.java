package com.github.mayconr.juoserver.game.skill.flow.lock;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.event.SkillLocked;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;

import java.util.ArrayList;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class SendSkillsLockStep extends AbstractFlowStep<SendSkillsLockContext> {

    private final EventBus eventBus;

    public SendSkillsLockStep(EventBus eventBus) {
        super("send-skills-lock");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(SendSkillsLockContext context) {
        var player = context.getPlayer();
        var skills = context.getSkills();
        var skillList = new ArrayList<SkillValue>();
        for (SkillValue skill : skills) {
            final var newValue = player.getSkills().updateSkill(skill.getSkillId(), skill.getLock());
            skillList.add(newValue);
        }
        eventBus.publish(new SkillLocked(player, skillList));
        return StepResult.success();
    }
}
