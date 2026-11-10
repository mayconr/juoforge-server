package com.github.mayconr.juoserver.game.container.flow.query;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class GetContainerItemsContext extends AbstractSyncFlowContext<List<UOItem>> {
    private final Integer containerSerial;
    private final Predicate<UOItem> predicate;
    private final boolean searchNestedContainers;
    private final List<UOItem> items = new ArrayList<>();
    @Setter
    private UOContainer container;
}
