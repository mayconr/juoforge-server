package com.github.mayconr.juoserver.game.container.flow.add;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.mobile.shared.MobileStatusContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class AddContainerItemContext extends AbstractSyncFlowContext<UOItem> implements MobileStatusContext {
    private final UOContainer container;
    private final UOItem item;
    private final boolean tryStack;
    private final boolean notifyClient;
    @Setter
    private UOMobile mobile;
}
