package com.github.mayconr.juoserver.game.mobile.flow.gold;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class RecalculateGoldContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile mobile;
    @Setter private int oldBalance;
}
