package com.github.mayconr.juoserver.game.container.flow.query;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class ResolveQueryContainerStep extends AbstractFlowStep<GetContainerItemsContext> {
    private final WorldStorage storage;

    public ResolveQueryContainerStep(WorldStorage storage) {
        super("resolve-query-container");
        this.storage = storage;
    }

    @Override
    public StepResult execute(GetContainerItemsContext context) {
        if (context.getContainerSerial() == null || context.getPredicate() == null) {
            return StepResult.failure("INVALID_CONTAINER_QUERY", "Container serial and predicate are required");
        }
        var container = storage.getContainer(context.getContainerSerial()).orElse(null);
        if (container == null) {
            return StepResult.failure("CONTAINER_NOT_FOUND", "Container not found: " + context.getContainerSerial());
        }
        context.setContainer(container);
        return StepResult.success();
    }
}
