package com.github.mayconr.juoserver.game.ui.flow.status;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class StatusGumpFlowDefinition {
    private StatusGumpFlowDefinition() { }

    public static Flow<StatusGumpContext> build(EventBus events, RealmStorage storage, WorldContainer containers, WorldMobile mobiles) {
        return FlowFactory.<StatusGumpContext>builder()
                .step(new FindStatusMobileStep(storage))
                .step(new CalculateStatusWeightStep(storage, containers))
                .step(new CalculateStatusMaxWeightStep(mobiles))
                .step(new NotifyStatusGumpRequestedStep(events))
                .build();
    }
}
