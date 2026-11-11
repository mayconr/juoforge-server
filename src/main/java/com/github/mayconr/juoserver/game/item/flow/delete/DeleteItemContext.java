package com.github.mayconr.juoserver.game.item.flow.delete;

import com.github.mayconr.juoserver.game.mobile.shared.MobileStatusContext;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class DeleteItemContext extends AbstractSyncFlowContext<Void> implements MobileStatusContext {
    private final UOItem item;
    /** Loaded container the item was detached from, or null when it was not in a container. */
    @Setter
    private UOContainer container;
    /** Mobile wearing the detached container, whose status reflects the new weight. */
    @Setter
    private UOMobile mobile;

    public boolean isDetachedFromContainer() {
        return container != null;
    }
}
