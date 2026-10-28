package com.github.mayconr.juoserver.game.skill.flow.lock;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class SendSkillsLockFlowDefinition {
    private SendSkillsLockFlowDefinition() { }

    public static Flow<SendSkillsLockContext> build(EventBus events) {
        return FlowFactory.<SendSkillsLockContext>builder()
                .step(new SendSkillsLockStep(events)).build();
    }
}
