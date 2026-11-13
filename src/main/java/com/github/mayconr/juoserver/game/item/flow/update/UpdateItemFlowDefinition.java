package com.github.mayconr.juoserver.game.item.flow.update;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class UpdateItemFlowDefinition {
    private UpdateItemFlowDefinition() { }

    public static Flow<UpdateItemContext> build(WorldStorage storage, EventBus events) {
        return FlowFactory.<UpdateItemContext>builder()
                .step(new ApplyItemChangeStep(storage, events))
                .build();
    }
}
