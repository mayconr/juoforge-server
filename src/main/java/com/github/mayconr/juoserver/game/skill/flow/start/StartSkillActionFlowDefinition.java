package com.github.mayconr.juoserver.game.skill.flow.start;

import com.github.mayconr.juoserver.game.interaction.WorldInteraction;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class StartSkillActionFlowDefinition {
    private StartSkillActionFlowDefinition() {}

    public static Flow<StartSkillActionContext> build(
            UOFileReader fileReader, SkillModule skills, WorldMessage messages, WorldMobile mobiles,
            WorldInteraction interaction) {
        return FlowFactory.<StartSkillActionContext>builder()
                .step(new ValidateSkillActionStep(fileReader))
                .step(new ResolveSkillSwingsStep(mobiles))
                .step(new CaptureSkillActionOriginStep())
                .step(new CalculateSkillActionDurationStep())
                .step(new AnimateSkillActionStep(interaction))
                .step(new ScheduleSkillActionStep(skills, messages))
                .build();
    }
}
