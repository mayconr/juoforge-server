package com.github.mayconr.juoserver.game.item.flow.drop.dropitem;

import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResolveItemStep extends AbstractFlowStep<DropItemContext> {

    private final RealmStorage storage;

    public ResolveItemStep(RealmStorage storage) {
        super("resolve_item");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        int serial = context.getDropItem().getSerialId();

        return storage.getItem(serial)
                .map(item -> {
                    context.setItem(item);
                    return StepResult.success();
                })
                .orElseGet(() -> StepResult.failure("Item not found for serial " + serial));
    }
}
