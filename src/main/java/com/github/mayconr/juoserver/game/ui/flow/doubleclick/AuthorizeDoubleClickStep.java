package com.github.mayconr.juoserver.game.ui.flow.doubleclick;

import com.github.mayconr.juoserver.game.ui.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.game.ui.policy.DoubleClickRequest;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class AuthorizeDoubleClickStep extends AbstractFlowStep<DoubleClickContext> {
    private final DoubleClickPolicy policy;

    public AuthorizeDoubleClickStep(DoubleClickPolicy policy) {
        super("authorize-double-click");
        this.policy = policy;
    }

    @Override
    public StepResult execute(DoubleClickContext context) {
        return policy.allows(new DoubleClickRequest(context.getPlayer(), context.getRequest().getSerialId()))
                ? StepResult.success() : StepResult.stop("DOUBLE_CLICK_DENIED", "Double click denied by policy");
    }
}
