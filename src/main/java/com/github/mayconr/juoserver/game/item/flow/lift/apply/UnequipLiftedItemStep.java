package com.github.mayconr.juoserver.game.item.flow.lift.apply;

import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Takes an equipped item off the player when it is lifted from the paperdoll. */
public final class UnequipLiftedItemStep extends AbstractFlowStep<LiftItemContext> {
    private final MobileModule mobiles;

    public UnequipLiftedItemStep(MobileModule mobiles) {
        super("unequip-lifted-item");
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(LiftItemContext context) {
        if (mobiles.unequipItem(context.getPlayer(), context.getItem())) {
            return StepResult.success();
        }
        return context.reject(LiftRejectReason.CANNOT_LIFT,
                "Item " + context.getItem().getSerialId() + " cannot be unequipped");
    }
}
