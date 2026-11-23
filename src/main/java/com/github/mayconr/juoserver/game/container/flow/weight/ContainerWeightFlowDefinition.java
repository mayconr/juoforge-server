package com.github.mayconr.juoserver.game.container.flow.weight;

import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class ContainerWeightFlowDefinition {
    private ContainerWeightFlowDefinition() {}

    public static Flow<ContainerWeightContext> build(WorldStorage storage) {
        return FlowFactory.<ContainerWeightContext>builder()
                .step(new ValidateContainerWeightRequestStep())
                .step(new CollectWeighedContentsStep(storage))
                .step(new SumContainerWeightStep())
                .build();
    }
}
