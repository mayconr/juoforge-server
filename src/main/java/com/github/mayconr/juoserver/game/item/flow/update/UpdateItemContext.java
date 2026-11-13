package com.github.mayconr.juoserver.game.item.flow.update;

import com.github.mayconr.juoserver.game.item.ItemChange;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class UpdateItemContext extends AbstractSyncFlowContext<Void> {
    private final UOItem item;
    private final ItemChange change;
}
