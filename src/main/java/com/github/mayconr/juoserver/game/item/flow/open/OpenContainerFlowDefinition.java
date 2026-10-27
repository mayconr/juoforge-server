package com.github.mayconr.juoserver.game.item.flow.open;

import com.github.mayconr.juoserver.game.loot.WorldLoot;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class OpenContainerFlowDefinition {
    private OpenContainerFlowDefinition() { }

    public static Flow<OpenContainerContext> build(RealmStorage storage, EventBus eventBus, WorldLoot loot) {
        return FlowFactory.<OpenContainerContext>builder()
                .step(new OpenContainerStep(storage, eventBus, loot)).build();
    }
}
