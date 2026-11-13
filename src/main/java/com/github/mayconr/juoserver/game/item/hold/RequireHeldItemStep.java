package com.github.mayconr.juoserver.game.item.hold;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Accepts a request that places an item only when the player is holding that same item. */
public final class RequireHeldItemStep<T extends AbstractContext & HeldItemRequest> extends AbstractFlowStep<T> {
    public static final String NOT_HELD = "ITEM_NOT_HELD";

    private final HeldItems heldItems;

    public RequireHeldItemStep(HeldItems heldItems) {
        super("require-held-item");
        this.heldItems = heldItems;
    }

    @Override
    public StepResult execute(T context) {
        int serial = context.requestedItemSerial();
        if (heldItems.isHolding(context.getPlayer(), serial)) {
            return StepResult.success();
        }
        return StepResult.failure(NOT_HELD, "Player is not holding item " + serial);
    }
}
