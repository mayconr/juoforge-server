package com.github.mayconr.juoserver.game.npc.flow.removal;

import com.github.mayconr.juoserver.game.flow.NpcRemovalFlowDefinition.NpcRemovalContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateNpcRemoval extends AbstractFlowStep<NpcRemovalContext> {
    public ValidateNpcRemoval(int order) {
        super("ValidateNpcRemoval", order, FlowPhase.CORE);
    }

    @Override
    public StepResult execute(NpcRemovalContext context) {

        return StepResult.success();
    }
}
