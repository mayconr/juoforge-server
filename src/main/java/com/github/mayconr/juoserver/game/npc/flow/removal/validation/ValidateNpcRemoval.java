package com.github.mayconr.juoserver.game.npc.flow.removal.validation;

import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateNpcRemoval extends AbstractFlowStep<NpcRemovalContext> {
    public ValidateNpcRemoval() {
        super("ValidateNpcRemoval");
    }

    @Override
    public StepResult execute(NpcRemovalContext context) {

        return StepResult.success();
    }
}
