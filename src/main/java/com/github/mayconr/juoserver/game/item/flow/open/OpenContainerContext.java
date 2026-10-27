package com.github.mayconr.juoserver.game.item.flow.open;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class OpenContainerContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final Container container;
}
