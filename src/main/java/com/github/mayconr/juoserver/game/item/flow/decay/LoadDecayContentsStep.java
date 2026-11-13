package com.github.mayconr.juoserver.game.item.flow.decay;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.LinkedHashMap;

/** Loads direct contents from persistence and includes items added since the last save. */
public final class LoadDecayContentsStep extends AbstractFlowStep<DecayItemContext> {
    private final WorldStorage storage;

    public LoadDecayContentsStep(WorldStorage storage) {
        super("load-decay-contents");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DecayItemContext context) {
        if (!(context.getItem() instanceof UOContainer container)) {
            return StepResult.success();
        }
        return StepResult.async(storage.items().loadContainerItems(container).thenApply(loaded -> {
            var contents = new LinkedHashMap<Integer, UOItem>();
            loaded.forEach(item -> contents.put(item.getSerialId(), item));
            for (var serial : container.getContainerItems()) {
                var item = storage.items().get(serial)
                        .orElseThrow(() -> new IllegalStateException("Container item not found: " + serial));
                contents.put(serial, item);
            }
            context.setContents(contents.values().stream().toList());
            return StepResult.success();
        }));
    }
}
