package com.github.mayconr.juoserver.game.skill.flow.update;

import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class UpdateSkillActionsFlowDefinition {
    private UpdateSkillActionsFlowDefinition() {}

    public static Flow<UpdateSkillActionsContext> build(SkillModule skills) {
        return FlowFactory.<UpdateSkillActionsContext>builder()
                .step(new ValidateSkillActionsUpdateStep())
                .step(new AdvanceSkillActionsStep(skills))
                .step(new CancelMovedSkillActionsStep(skills))
                .step(new ExecuteSkillActionsStep(skills))
                .build();
    }
}
