package com.github.mayconr.juoserver.game.container.flow.weight;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Collects all loaded contents of the container, including every nested container. */
public final class CollectWeighedContentsStep extends AbstractFlowStep<ContainerWeightContext> {
    private final WorldStorage storage;

    public CollectWeighedContentsStep(WorldStorage storage) {
        super("collect-weighed-contents");
        this.storage = storage;
    }

    @Override
    public StepResult execute(ContainerWeightContext context) {
        var visited = new HashSet<Integer>();
        var pending = new ArrayDeque<UOContainer>();
        visited.add(context.getContainer().getSerialId());
        pending.add(context.getContainer());
        while (!pending.isEmpty()) {
            var result = collect(context, pending.removeFirst(), visited, pending);
            if (result.flowFailed()) {
                return result;
            }
        }
        return StepResult.success();
    }

    private StepResult collect(
            ContainerWeightContext context,
            UOContainer container,
            Set<Integer> visited,
            ArrayDeque<UOContainer> pending) {
        for (var serial : container.getContainerItems()) {
            var item = storage.items().get(serial).orElse(null);
            if (item == null) {
                return StepResult.failure("CONTAINER_ITEM_NOT_LOADED", "Cannot calculate weight: item " + serial
                        + " from container " + container.getSerialId() + " is not loaded");
            }
            if (item instanceof UOContainer nested) {
                if (!visited.add(nested.getSerialId())) {
                    return StepResult.failure("CONTAINER_CYCLE",
                            "Repeated container or cycle at serial " + nested.getSerialId());
                }
                pending.add(nested);
            }
            context.getContents().add(item);
        }
        return StepResult.success();
    }
}
