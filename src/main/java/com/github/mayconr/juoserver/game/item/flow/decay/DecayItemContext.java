package com.github.mayconr.juoserver.game.item.flow.decay;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** One ground item considered by the periodic decay scan. */
@Getter
@RequiredArgsConstructor
public final class DecayItemContext extends AbstractSyncFlowContext<Void> {
    private final UOItem item;
    @Setter
    private List<UOItem> contents = List.of();
}
