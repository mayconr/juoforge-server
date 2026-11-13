package com.github.mayconr.juoserver.game.item.flow.decay;

import com.github.mayconr.juoserver.game.item.decay.ItemDecayState;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.function.LongSupplier;

/** Revalidates the index candidate before loading its contents or changing the world. */
public final class ValidateDecayItemStep extends AbstractFlowStep<DecayItemContext> {
    private final WorldStorage storage;
    private final HeldItems heldItems;
    private final LongSupplier clock;
    private final double durationSeconds;
    private final ItemModule items;

    public ValidateDecayItemStep(
            WorldStorage storage,
            ItemModule items,
            HeldItems heldItems,
            LongSupplier clock,
            double durationSeconds
    ) {
        super("validate-decay-item");
        this.storage = storage;
        this.heldItems = heldItems;
        this.clock = clock;
        this.durationSeconds = durationSeconds;
        this.items = items;
    }

    @Override
    public StepResult execute(DecayItemContext context) {
        return canDecay(context.getItem()) ? StepResult.success() : StepResult.stop();
    }

    public boolean canDecay(UOItem item) {
        return storage.items().get(item.getSerialId()).orElse(null) == item
                && item.isOnTheGround() && item.isMovable() && !heldItems.isHeld(item)
                && ItemDecayState.isDue(items, item, clock.getAsLong(), durationSeconds);
    }
}
