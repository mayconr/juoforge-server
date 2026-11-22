package com.github.mayconr.juoserver.game.item.flow.flip;

import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/**
 * Moves the model to the next one, or back to the template model when it is already the next one.
 * The current model is read and replaced under the item monitor, so concurrent flips alternate correctly.
 */
final class FlipItemModelStep extends AbstractFlowStep<FlipItemContext> {
    /** Object info packets (0x1A) identify graphics from 0x4000 onwards as multis instead of items. */
    private static final int MAX_GROUND_MODEL_ID = 0x3FFF;
    private static final int MAX_MODEL_ID = 0xFFFF;

    FlipItemModelStep() {
        super("flip-item-model");
    }

    @Override
    public StepResult execute(FlipItemContext context) {
        var item = context.getItem();
        synchronized (item) {
            int base = item.getTemplate().modelId();
            int current = item.getModelId();
            if (current != base && current != base + 1) {
                return StepResult.failure("ITEM_FLIP_MODEL", "Item model " + current
                        + " is neither the template model " + base + " nor the next one");
            }
            int flipped = current == base ? base + 1 : base;
            if (flipped > maxModelId(item)) {
                return StepResult.failure("ITEM_FLIP_MODEL", "Flipped model " + flipped + " is out of range");
            }
            item.setModelId(flipped);
            return StepResult.success();
        }
    }

    private int maxModelId(UOItem item) {
        return item.getCurrentLocation() instanceof GroundLocation ? MAX_GROUND_MODEL_ID : MAX_MODEL_ID;
    }
}
