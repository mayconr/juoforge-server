package com.github.mayconr.juoserver.game.mobile.flow.gold;

import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class RecalculateGoldFlowDefinition {
    private RecalculateGoldFlowDefinition() { }

    public static Flow<RecalculateGoldContext> build(Wallet wallet, EventBus events, WorldUI ui) {
        return FlowFactory.<RecalculateGoldContext>builder()
                .step(new CalculateGoldStep(wallet))
                .step(new NotifyGoldStep(events, ui))
                .build();
    }
}
