package com.github.mayconr.juoserver.game.container.flow.weight;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ContainerWeightContext extends AbstractSyncFlowContext<Long> {
    private final UOContainer container;
    private final List<UOItem> contents = new ArrayList<>();
}
