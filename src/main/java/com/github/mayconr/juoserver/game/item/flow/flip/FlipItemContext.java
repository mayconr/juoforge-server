package com.github.mayconr.juoserver.game.item.flow.flip;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;

import java.util.Objects;

/** State of a flip: the item and, when it is inside one, the container to refresh. */
public final class FlipItemContext extends AbstractSyncFlowContext<Void> {
    private final UOItem item;
    private UOContainer container;

    public FlipItemContext(UOItem item) {
        this.item = Objects.requireNonNull(item, "Item is required");
    }

    public UOItem getItem() {
        return item;
    }

    UOContainer getContainer() {
        return container;
    }

    void resolveContainer(UOContainer container) {
        this.container = container;
    }
}
