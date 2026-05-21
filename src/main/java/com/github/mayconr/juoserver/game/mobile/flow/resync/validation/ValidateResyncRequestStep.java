package com.github.mayconr.juoserver.game.mobile.flow.resync.validation;

import com.github.mayconr.juoserver.game.mobile.flow.resync.ResyncContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateResyncRequestStep extends AbstractFlowStep<ResyncContext> {
    public ValidateResyncRequestStep() {
        super("ValidateResyncRequest");
    }

    @Override
    public StepResult execute(ResyncContext context) {
        if (context.getPlayer() == null) {
            return StepResult.failure("Player is null");
        }
        if (context.getResyncAck() == null) {
            return StepResult.failure("ResyncAck is null");
        }
        return StepResult.success();
    }
}
