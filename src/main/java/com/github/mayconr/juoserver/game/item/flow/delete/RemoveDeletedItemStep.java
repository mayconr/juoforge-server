package com.github.mayconr.juoserver.game.item.flow.delete;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Removes the item from the world storage cache and indexes and schedules its removal on save. */
public final class RemoveDeletedItemStep extends AbstractFlowStep<DeleteItemContext> {
    private final WorldStorage storage;

    public RemoveDeletedItemStep(WorldStorage storage) {
        super("remove-deleted-item");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DeleteItemContext context) {
        storage.items().delete(context.getItem());
        return StepResult.success();
    }
}
