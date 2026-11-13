package com.github.mayconr.juoserver.game.item.flow.lift.apply;

import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Puts the item on the player's cursor, unless another player lifted it first. */
public final class HoldItemStep extends AbstractFlowStep<LiftItemContext> {
    private final HeldItems heldItems;
    private final WorldStorage storage;

    public HoldItemStep(HeldItems heldItems, WorldStorage storage) {
        super("hold-item");
        this.heldItems = heldItems;
        this.storage = storage;
    }

    @Override
    public StepResult execute(LiftItemContext context) {
        synchronized (heldItems) {
            if (storage.items().get(context.getItem().getSerialId()).orElse(null) != context.getItem()) {
                return context.reject(LiftRejectReason.CANNOT_LIFT, "Item no longer exists");
            }
            return hold(context);
        }
    }

    private StepResult hold(LiftItemContext context) {
        var player = context.getPlayer();
        if (heldItems.hold(player, context.getItem())) {
            context.setHeld(true);
            return StepResult.success();
        }
        if (heldItems.isHoldingAny(player)) {
            return context.reject(LiftRejectReason.ARE_HOLDING, "Player is already holding an item");
        }
        return context.reject(LiftRejectReason.CANNOT_LIFT, "Item is held by another player");
    }
}
