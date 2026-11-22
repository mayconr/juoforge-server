package com.github.mayconr.juoserver.game.ui.flow.doubleclick;

import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.game.ui.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class DoubleClickFlowDefinition {
    private DoubleClickFlowDefinition() { }

    public static Flow<DoubleClickContext> build(WorldStorage storage, DoubleClickPolicy policy,
                                                 WorldItem items, WorldContainer containers, WorldUI ui) {
        return FlowFactory.<DoubleClickContext>builder()
                .step(new AuthorizeDoubleClickStep(policy))
                .step(new UseClickedItemStep(storage, items, containers), context -> !context.targetsMobile())
                .step(new OpenClickedPaperdollStep(ui), DoubleClickContext::targetsMobile)
                .build();
    }
}
