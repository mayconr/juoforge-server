package com.github.mayconr.juoserver.game.container.flow.count;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collects the contents of the root container and, when requested, of every nested container.
 * With warmup, persisted contents are loaded into the item cache before being collected.
 */
public final class CollectCountableItemsStep extends AbstractFlowStep<CountContainerItemsContext> {
    private final WorldStorage storage;

    public CollectCountableItemsStep(WorldStorage storage) {
        super("collect-countable-items");
        this.storage = storage;
    }

    @Override
    public StepResult execute(CountContainerItemsContext context) {
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
            CountContainerItemsContext context,
            UOContainer container,
            Set<Integer> visited,
            ArrayDeque<UOContainer> pending) {
        var contents = new LinkedHashMap<Integer, UOItem>();
        if (context.getOptions().warmup()) {
            warmUp(container, contents);
        }
        var missing = addCachedContents(container, contents);
        if (missing != null) {
            return StepResult.failure("CONTAINER_ITEM_NOT_LOADED",
                    "Item " + missing + " from container " + container.getSerialId() + " is not loaded");
        }
        for (var item : contents.values()) {
            context.getItems().add(item);
            if (context.getOptions().searchNestedContainers() && item instanceof UOContainer nested) {
                if (!visited.add(nested.getSerialId())) {
                    return StepResult.failure("CONTAINER_CYCLE",
                            "Repeated container or cycle at serial " + nested.getSerialId());
                }
                pending.add(nested);
            }
        }
        return StepResult.success();
    }

    private void warmUp(UOContainer container, Map<Integer, UOItem> contents) {
        for (var item : storage.items().loadContainerItems(container).join()) {
            contents.put(item.getSerialId(), item);
        }
    }

    /** Adds registered contents not yet collected and returns the first serial that is not loaded. */
    private Integer addCachedContents(UOContainer container, Map<Integer, UOItem> contents) {
        for (var serial : List.copyOf(container.getContainerItems())) {
            if (contents.containsKey(serial)) {
                continue;
            }
            var item = storage.items().get(serial).orElse(null);
            if (item == null) {
                return serial;
            }
            contents.put(serial, item);
        }
        return null;
    }
}
