package com.github.mayconr.juoserver.game.item.flow.delete;

import com.github.mayconr.juoserver.game.mobile.shared.NotifyMobileStatusStep;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/** Deletes an item, detaching it from its parent container before removing it from the world. */
public final class DeleteItemFlowDefinition {
    private DeleteItemFlowDefinition() { }

    public static Flow<DeleteItemContext> build(WorldStorage storage, EventBus events, WorldUI ui) {
        return FlowFactory.<DeleteItemContext>builder()
                .step(new DetachDeletedItemStep(storage))
                .step(new RemoveDeletedItemStep(storage))
                .step(new PublishItemDeletedStep(events))
                .step(new FindDetachedContainerOwnerStep(storage), DeleteItemContext::isDetachedFromContainer)
                .step(new NotifyMobileStatusStep<>(ui), DeleteItemContext::isDetachedFromContainer)
                .build();
    }
}
