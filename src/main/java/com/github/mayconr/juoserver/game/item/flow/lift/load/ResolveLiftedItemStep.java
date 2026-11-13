package com.github.mayconr.juoserver.game.item.flow.lift.load;

import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Loads the item named by the lift request. */
public final class ResolveLiftedItemStep extends AbstractFlowStep<LiftItemContext> {
    private final WorldStorage storage;

    public ResolveLiftedItemStep(WorldStorage storage) {
        super("resolve-lifted-item");
        this.storage = storage;
    }

    @Override
    public StepResult execute(LiftItemContext context) {
        int serial = context.getRequest().getSerialId();
        var item = storage.items().get(serial);
        if (item.isEmpty()) {
            return context.reject(LiftRejectReason.CANNOT_LIFT, "Item not found for serial " + serial);
        }
        context.setItem(item.get());
        return StepResult.success();
    }
}
