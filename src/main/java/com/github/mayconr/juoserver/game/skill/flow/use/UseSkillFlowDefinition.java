package com.github.mayconr.juoserver.game.skill.flow.use;

import com.github.mayconr.juoserver.game.skill.flow.use.dispatch.DispatchSkillStep;
import com.github.mayconr.juoserver.game.skill.flow.use.validation.ValidateSkillStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class UseSkillFlowDefinition {

    public static Flow<UseSkillContext> build(FlowRegistryFactory.GameModules modules, FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<UseSkillContext>builder()
                .step(new ValidateSkillStep(infra.fileReader()))
                .step(new DispatchSkillStep(infra.eventBus(), infra.fileReader()))
                .build();
    }

}
