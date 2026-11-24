package com.github.mayconr.juoserver.game.item.flow.lift.apply;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class SplitLiftedStackStep extends AbstractFlowStep<LiftItemContext> {
    private final ItemModule items;

    public SplitLiftedStackStep(ItemModule items) {
        super("split-lifted-stack");
        this.items = items;
    }

    public static boolean isPartialLift(LiftItemContext context) {
        return context.getItem().getAmount() > 1
                && context.getRequest().getAmount() < context.getItem().getAmount();
    }

    @Override
    public StepResult execute(LiftItemContext context) {
        var result = items.splitHeldStack(context.getPlayer(), context.getItem(), context.getRequest().getAmount());
        if (result.flowFailed()) {
            return context.reject(LiftRejectReason.CANNOT_LIFT, result.reason());
        }
        return StepResult.success();
    }
}
