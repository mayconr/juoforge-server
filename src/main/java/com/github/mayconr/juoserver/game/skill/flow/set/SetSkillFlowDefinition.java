package com.github.mayconr.juoserver.game.skill.flow.set;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class SetSkillFlowDefinition {
    private SetSkillFlowDefinition() { }

    public static Flow<SetSkillContext> build(EventBus events) {
        return FlowFactory.<SetSkillContext>builder().step(new SetSkillStep(events)).build();
    }
}
