package com.github.mayconr.juoserver.game.container.flow.load;

import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class LoadContainerContentsFlowDefinition {
    private LoadContainerContentsFlowDefinition() { }

    public static Flow<LoadContainerContentsContext> build(WorldStorage storage) {
        return FlowFactory.<LoadContainerContentsContext>builder()
                .step(new LoadContainerContentsStep(storage.items()))
                .build();
    }
}
