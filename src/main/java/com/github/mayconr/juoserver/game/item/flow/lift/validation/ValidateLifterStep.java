package com.github.mayconr.juoserver.game.item.flow.lift.validation;

import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Only a living player whose cursor is free can lift an item. */
public final class ValidateLifterStep extends AbstractFlowStep<LiftItemContext> {
    private final HeldItems heldItems;

    public ValidateLifterStep(HeldItems heldItems) {
        super("validate-lifter");
        this.heldItems = heldItems;
    }

    @Override
    public StepResult execute(LiftItemContext context) {
        var player = context.getPlayer();
        if (!player.isAlive()) {
            return context.reject(LiftRejectReason.INSPECIFIC, "Dead players cannot lift items");
        }
        if (heldItems.isHoldingAny(player)) {
            return context.reject(LiftRejectReason.ARE_HOLDING, "Player is already holding an item");
        }
        return StepResult.success();
    }
}
