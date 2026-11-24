package com.github.mayconr.juoserver.game.mobile.flow.gold;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import java.util.function.IntConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class RecalculateGoldContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile mobile;
    private final IntConsumer updateBalance;
    private final boolean notifyClient;
    @Setter private int oldBalance;

    public RecalculateGoldContext(UOMobile mobile, IntConsumer updateBalance) {
        this(mobile, updateBalance, true);
    }
}
