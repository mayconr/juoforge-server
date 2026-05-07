package com.github.mayconr.juoserver.game.interaction.flow.target.send.validation;

import com.github.mayconr.juoserver.game.interaction.flow.target.send.SendTargetContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateTargetStep extends AbstractFlowStep<SendTargetContext> {
    public ValidateTargetStep() {
        super("ValidateTarget");
    }

    @Override
    public StepResult execute(SendTargetContext context) {
        return StepResult.success();
    }
}
