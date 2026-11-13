package com.github.mayconr.juoserver.game.item.flow.decay;

import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

/** Decay has its own eligibility and spilling rules; deletion reuses the existing item operation. */
public final class DecayItemFlowDefinition {
    private DecayItemFlowDefinition() {
    }

    public static Flow<DecayItemContext> build(GameModules modules, GameInfra infra) {
        var items = modules.item();
        var validation = new ValidateDecayItemStep(infra.storage(), items, items.heldItems(),
                items::decayNow, infra.settings().items().decay().durationSeconds());
        return FlowFactory.<DecayItemContext>builder()
                .step(validation)
                .step(new LoadDecayContentsStep(infra.storage()))
                .step(new ApplyDecayStep(infra.storage(), items, infra.eventBus(), validation))
                .build();
    }
}
