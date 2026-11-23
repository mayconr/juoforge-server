package com.github.mayconr.juoserver.game.container.flow.open;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class OpenContainerContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final Container container;
    private final boolean equipBeforeOpening;
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private CompletableFuture<List<UOItem>> contents;

    public OpenContainerContext(UOPlayer player, Container container) {
        this(player, container, false);
    }
}
