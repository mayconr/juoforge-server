package com.github.mayconr.juoserver.game.container.flow.query;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class CollectContainerItemsStep extends AbstractFlowStep<GetContainerItemsContext> {
    private final RealmStorage storage;

    public CollectContainerItemsStep(RealmStorage storage) {
        super("collect-container-items");
        this.storage = storage;
    }

    @Override
    public StepResult execute(GetContainerItemsContext context) {
        var visited = new HashSet<Integer>();
        var pending = new ArrayDeque<UOContainer>();
        visited.add(context.getContainerSerial());
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
            GetContainerItemsContext context,
            UOContainer container,
            Set<Integer> visited,
            ArrayDeque<UOContainer> pending) {
        for (var serial : container.getContainerItems()) {
            if (!visited.add(serial)) {
                continue;
            }
            var item = storage.getItem(serial).orElse(null);
            if (item == null) {
                return StepResult.failure("CONTAINER_ITEM_NOT_FOUND", "Item not found: " + serial);
            }
            context.getItems().add(item);
            if (context.isSearchNestedContainers() && item instanceof UOContainer nested) {
                pending.add(nested);
            }
        }
        return StepResult.success();
    }
}
