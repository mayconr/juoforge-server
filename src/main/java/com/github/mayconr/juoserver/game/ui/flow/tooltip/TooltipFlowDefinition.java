package com.github.mayconr.juoserver.game.ui.flow.tooltip;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class TooltipFlowDefinition {
    private TooltipFlowDefinition() { }

    public static Flow<TooltipContext> build(EventBus events, RealmStorage storage) {
        return FlowFactory.<TooltipContext>builder()
                .step(new RequestTooltipStep(events, storage)).build();
    }
}
