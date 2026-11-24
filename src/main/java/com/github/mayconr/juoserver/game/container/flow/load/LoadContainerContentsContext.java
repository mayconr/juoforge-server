package com.github.mayconr.juoserver.game.container.flow.load;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Loads the persisted direct children of a container; the result completes once they are registered. */
@Getter
@RequiredArgsConstructor
public final class LoadContainerContentsContext extends AbstractSyncFlowContext<CompletableFuture<List<UOItem>>> {
    private final UOContainer container;
}
