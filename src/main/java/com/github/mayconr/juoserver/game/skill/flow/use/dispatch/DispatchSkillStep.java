package com.github.mayconr.juoserver.game.skill.flow.use.dispatch;

import com.github.mayconr.juoserver.game.model.event.UseSkillRequested;
import com.github.mayconr.juoserver.game.skill.flow.use.UseSkillContext;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispatchSkillStep extends AbstractFlowStep<UseSkillContext> {

    private final EventBus eventBus;
    private final UOFileReader fileReader;

    public DispatchSkillStep(EventBus eventBus, UOFileReader fileReader) {
        super("DispatchSkill");
        this.eventBus = eventBus;
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(UseSkillContext context) {
        final var player = context.getPlayer();
        final var skill = fileReader.getSkill(context.getSkillId())
                .orElseThrow(()->new IllegalStateException("Skill [skillId={}] is not found"));

        if (log.isDebugEnabled()) {
            log.debug("Skill [name={}] use requested by [{}-{}]", skill.name(), player.getSerialId(), player.getName());
        }

        eventBus.publish(new UseSkillRequested(player, skill));
        return StepResult.success();
    }
}
