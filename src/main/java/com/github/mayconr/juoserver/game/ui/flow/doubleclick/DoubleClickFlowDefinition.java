package com.github.mayconr.juoserver.game.ui.flow.doubleclick;

import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.ui.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class DoubleClickFlowDefinition {
    private DoubleClickFlowDefinition() { }

    public static Flow<DoubleClickContext> build(RealmStorage storage,
                                                DoubleClickPolicy policy, WorldItem items, WorldContainer containers) {
        return FlowFactory.<DoubleClickContext>builder()
                .step(new AuthorizeDoubleClickStep(policy))
                .step(new UseClickedItemStep(storage, items, containers), context -> !context.targetsMobile())
                .build();
    }
}
