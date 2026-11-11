package com.github.mayconr.juoserver.game.skill.flow.stop;

import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class StopSkillActionFlowDefinition {
    private StopSkillActionFlowDefinition() {}

    public static Flow<StopSkillActionContext> build(SkillModule skills) {
        return FlowFactory.<StopSkillActionContext>builder()
                .step(new RequestSkillActionStopStep(skills))
                .build();
    }
}
