package com.github.mayconr.juoserver.game.ui.flow.singleclick;

import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

final class ResolveClickedObjectStep extends AbstractFlowStep<SingleClickContext> {
    private final WorldStorage storage;

    ResolveClickedObjectStep(WorldStorage storage) {
        super("resolve-clicked-object");
        this.storage = storage;
    }

    @Override
    public StepResult execute(SingleClickContext context) {
        int serial = context.getRequest().getSerialId();
        if (SerialGenerator.isMobile(serial)) {
            storage.mobiles().get(serial).ifPresent(context::setObject);
        } else if (SerialGenerator.isItem(serial)) {
            storage.items().get(serial).ifPresent(context::setObject);
        }
        return StepResult.success();
    }
}
