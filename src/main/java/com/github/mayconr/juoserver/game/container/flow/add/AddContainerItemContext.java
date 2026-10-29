package com.github.mayconr.juoserver.game.container.flow.add;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AddContainerItemContext extends AbstractSyncFlowContext<UOItem> {
    private final UOContainer container;
    private final UOItem item;
    private final boolean tryStack;
    private final boolean notifyClient;
}
