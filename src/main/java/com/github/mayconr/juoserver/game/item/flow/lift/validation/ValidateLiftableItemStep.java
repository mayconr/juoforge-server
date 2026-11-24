package com.github.mayconr.juoserver.game.item.flow.lift.validation;

import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Validates that the item can be lifted and that a stack request has a positive quantity. */
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
        if (item.getAmount() > 1 && context.getRequest().getAmount() == 0) {
            return context.reject(LiftRejectReason.CANNOT_LIFT, "Stack lift requires a positive quantity");
        }
        return StepResult.success();
    }
}
