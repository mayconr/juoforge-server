package com.github.mayconr.juoserver.game.skill.flow.start;

import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.skill.SkillAction;
import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ScheduleSkillActionStep extends AbstractFlowStep<StartSkillActionContext> {
    private final SkillModule skills;
    private final WorldMessage messages;

    public ScheduleSkillActionStep(SkillModule skills, WorldMessage messages) {
        super("schedule-skill-action");
        this.skills = skills;
        this.messages = messages;
    }

    @Override
    public StepResult execute(StartSkillActionContext context) {
        var player = context.getPlayer();
        var options = context.getOptions();
        // TODO: Make the skill action interval configurable.
        var action = new SkillAction(
                player,
                context.getOrigin(),
                context.getSwings(),
                1.0,
                options.onSwing(),
                options.onComplete()
        );
        boolean reserved = skills.reserveSkillAction(player, action);
        context.complete(reserved);
        if (!reserved) {
            messages.send(player, MessageContent.localized("skill.action.already_active"));
            return StepResult.stop("SKILL_ACTION_BUSY", "Player already has an active skill action");
        }
        return StepResult.success();
    }
}
