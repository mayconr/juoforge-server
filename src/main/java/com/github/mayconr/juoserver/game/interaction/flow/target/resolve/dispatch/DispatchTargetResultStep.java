package com.github.mayconr.juoserver.game.interaction.flow.target.resolve.dispatch;

import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class DispatchTargetResultStep extends AbstractFlowStep<ResolveTargetContext> {
    public DispatchTargetResultStep() {
        super("DispatchTargetResult");
    }

    @Override
    public StepResult execute(ResolveTargetContext context) {

        var callback = context.getCallback();
        if (callback == null) {
            return StepResult.failure("Callback is null");
        }

        callback.accept(context.getTargetResult());

        return StepResult.success();
    }
}
