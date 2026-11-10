package com.github.mayconr.juoserver.game.container.flow.query;

import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class GetContainerItemsFlowDefinition {
    private GetContainerItemsFlowDefinition() {}

    public static Flow<GetContainerItemsContext> build(RealmStorage storage) {
        return FlowFactory.<GetContainerItemsContext>builder()
                .step(new ResolveQueryContainerStep(storage))
                .step(new CollectContainerItemsStep(storage))
                .step(new FilterContainerItemsStep())
                .build();
    }
}
