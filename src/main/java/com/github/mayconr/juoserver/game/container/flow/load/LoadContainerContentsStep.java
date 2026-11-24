package com.github.mayconr.juoserver.game.container.flow.load;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import java.util.List;

/**
 * Loads the persisted direct children into the item cache and registers them as container members.
 * No client notification or inventory event is published: the items already belong to the container.
 */
public final class LoadContainerContentsStep extends AbstractFlowStep<LoadContainerContentsContext> {
    private final Items items;

    public LoadContainerContentsStep(Items items) {
        super("load-container-contents");
        this.items = items;
    }

    @Override
    public StepResult execute(LoadContainerContentsContext context) {
        var container = context.getContainer();
        context.complete(items.loadContainerItems(container)
                .thenApply(loaded -> register(container, loaded)));
        return StepResult.success();
    }

    private static List<UOItem> register(UOContainer container, List<UOItem> loaded) {
        synchronized (container) {
            container.addItemsToContainer(loaded);
        }
        return loaded;
    }
}
