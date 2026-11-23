package com.github.mayconr.juoserver.game.container.flow.count;

import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class CountContainerItemsFlowDefinition {
    private CountContainerItemsFlowDefinition() {}

    public static Flow<CountContainerItemsContext> build(WorldStorage storage) {
        return FlowFactory.<CountContainerItemsContext>builder()
                .step(new ValidateCountItemsRequestStep())
                .step(new CollectCountableItemsStep(storage))
                .step(new SumMatchingItemsStep())
                .build();
    }
}
