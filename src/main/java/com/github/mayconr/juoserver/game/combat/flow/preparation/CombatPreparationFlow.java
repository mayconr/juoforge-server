package com.github.mayconr.juoserver.game.combat.flow.preparation;

import com.github.mayconr.juoserver.game.combat.flow.preparation.build.BuildSessionStep;
import com.github.mayconr.juoserver.game.combat.flow.preparation.resolver.ResolveTargetStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class CombatPreparationFlow {
    private CombatPreparationFlow() {
    }

    public static Flow<CombatPreparationContext> build(FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<CombatPreparationContext>builder()
                .step(new ResolveTargetStep(infra.storage()))
                .step(new BuildSessionStep(infra.eventBus()))
                .build();
    }
}
