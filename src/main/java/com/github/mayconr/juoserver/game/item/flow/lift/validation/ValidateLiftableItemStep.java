package com.github.mayconr.juoserver.game.item.flow.lift.validation;

import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/**
 * The item must be movable and lifted as a whole stack. Splitting a stack on lift is not supported yet, so
 * a request for part of a stack is refused instead of moving the whole stack.
 */
public final class ValidateLiftableItemStep extends AbstractFlowStep<LiftItemContext> {

    public ValidateLiftableItemStep() {
        super("validate-liftable-item");
    }

    @Override
    public StepResult execute(LiftItemContext context) {
        var item = context.getItem();
        if (!item.isMovable()) {
            return context.reject(LiftRejectReason.CANNOT_LIFT, "Item " + item.getSerialId() + " is not movable");
        }
        int requested = context.getRequest().getAmount();
        if (!isWholeStack(requested, item.getAmount())) {
            return context.reject(LiftRejectReason.CANNOT_LIFT,
                    "Lifting " + requested + " of " + item.getAmount() + " would split the stack");
        }
        return StepResult.success();
    }

    private static boolean isWholeStack(int requested, int available) {
        return available <= 1 || requested >= available;
    }
}
