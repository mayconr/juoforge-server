package com.github.mayconr.juoserver.game.skill.flow.perform;

import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class PerformSkillFlowDefinition {
    private PerformSkillFlowDefinition() {}

    public static Flow<PerformSkillContext> build(UOFileReader fileReader, SkillModule skills) {
        return FlowFactory.<PerformSkillContext>builder()
                .step(new ValidatePerformSkillStep(fileReader))
                .step(new ScheduleSkillActionStep(skills))
                .build();
    }
}
