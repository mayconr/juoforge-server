package com.github.mayconr.juoserver.game.container.flow.count;

import com.github.mayconr.juoserver.game.container.CountItemsOptions;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class CountContainerItemsContext extends AbstractSyncFlowContext<Integer> {
    private final UOContainer container;
    private final String name;
    private final CountItemsOptions options;
    private final List<UOItem> items = new ArrayList<>();
}
