package com.github.mayconr.juoserver.game.ui.flow.singleclick;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class SingleClickFlowDefinition {
    private SingleClickFlowDefinition() { }

    public static Flow<SingleClickContext> build(WorldStorage storage, EventBus events) {
        return FlowFactory.<SingleClickContext>builder().step(new RequestObjectLabelStep(storage, events)).build();
    }
}
