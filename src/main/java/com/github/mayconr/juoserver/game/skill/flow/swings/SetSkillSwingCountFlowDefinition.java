package com.github.mayconr.juoserver.game.skill.flow.swings;

import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class SetSkillSwingCountFlowDefinition {
    private SetSkillSwingCountFlowDefinition() {}

    public static Flow<SetSkillSwingCountContext> build(WorldMobile mobiles) {
        return FlowFactory.<SetSkillSwingCountContext>builder()
                .step(new SetSkillSwingCountStep(mobiles))
                .build();
    }
}
