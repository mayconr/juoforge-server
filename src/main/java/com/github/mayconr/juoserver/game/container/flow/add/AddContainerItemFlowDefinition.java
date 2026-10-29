package com.github.mayconr.juoserver.game.container.flow.add;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.game.mobile.shared.NotifyMobileStatusStep;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class AddContainerItemFlowDefinition {
    private AddContainerItemFlowDefinition() { }

    public static Flow<AddContainerItemContext> build(RealmStorage storage, EventBus events, WorldUI ui) {
        return FlowFactory.<AddContainerItemContext>builder()
                .step(new PlaceContainerItemStep(storage))
                .step(new NotifyContainerItemStep(storage, events), AddContainerItemContext::isNotifyClient)
                .step(new FindContainerOwnerStep(storage), AddContainerItemContext::isNotifyClient)
                .step(new NotifyMobileStatusStep<>(ui), AddContainerItemContext::isNotifyClient)
                .build();
    }
}
