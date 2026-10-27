package com.github.mayconr.juoserver.game.ui.flow.skill;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class SkillGumpFlowDefinition {
    private SkillGumpFlowDefinition() { }

    public static Flow<SkillGumpContext> build(EventBus events, RealmStorage storage) {
        return FlowFactory.<SkillGumpContext>builder()
                .step(new SendSkillGumpStep(events, storage)).build();
    }
}
