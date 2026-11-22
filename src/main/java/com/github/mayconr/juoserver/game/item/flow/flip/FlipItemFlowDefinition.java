package com.github.mayconr.juoserver.game.item.flow.flip;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Alternates the model of a flippable item and refreshes it on the clients that can see it. */
public final class FlipItemFlowDefinition {
    private FlipItemFlowDefinition() {
    }

    public static Flow<FlipItemContext> build(WorldStorage storage, EventBus events) {
        return FlowFactory.<FlipItemContext>builder()
                .step(new ValidateFlippableItemStep())
                .step(new ResolveFlipContainerStep(storage))
                .step(new FlipItemModelStep())
                .step(new NotifyItemFlippedStep(events))
                .build();
    }
}
