package com.github.mayconr.juoserver.game.item.flow.equip.load;

import com.github.mayconr.juoserver.game.item.flow.equip.EquipHeldItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Loads the held item named by the equip request. */
public final class ResolveHeldItemStep extends AbstractFlowStep<EquipHeldItemContext> {
    private final WorldStorage storage;

    public ResolveHeldItemStep(WorldStorage storage) {
        super("resolve-held-item");
        this.storage = storage;
    }

    @Override
    public StepResult execute(EquipHeldItemContext context) {
        int serial = context.requestedItemSerial();
        var item = storage.items().get(serial);
        if (item.isEmpty()) {
            return StepResult.failure("ITEM_NOT_FOUND", "Item not found for serial " + serial);
        }
        context.setItem(item.get());
        return StepResult.success();
    }
}
