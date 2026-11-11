package com.github.mayconr.juoserver.game.skill.flow.perform;

import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class PerformSkillFlowDefinition {
    private PerformSkillFlowDefinition() {}

    public static Flow<PerformSkillContext> build(
            UOFileReader fileReader, SkillModule skills, WorldMessage messages, WorldMobile mobiles) {
        return FlowFactory.<PerformSkillContext>builder()
                .step(new ValidatePerformSkillStep(fileReader))
                .step(new ResolveSkillSwingsStep(mobiles))
                .step(new CaptureSkillActionOriginStep())
                .step(new ScheduleSkillActionStep(skills, messages))
                .build();
    }
}
