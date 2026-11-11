package com.github.mayconr.juoserver.game.ui.flow.paperdoll;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class OpenPaperdollFlowDefinition {
    private OpenPaperdollFlowDefinition() { }

    public static Flow<OpenPaperdollContext> build(EventBus events, WorldStorage storage) {
        return FlowFactory.<OpenPaperdollContext>builder()
                .step(new OpenPaperdollStep(events, storage)).build();
    }
}
