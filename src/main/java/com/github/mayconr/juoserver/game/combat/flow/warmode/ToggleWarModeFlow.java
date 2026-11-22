package com.github.mayconr.juoserver.game.combat.flow.warmode;

import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class ToggleWarModeFlow {
    private ToggleWarModeFlow() {
    }

    public static Flow<ToggleWarModeContext> build(FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<ToggleWarModeContext>builder()
                .step(new ApplyWarModeStep(infra.eventBus()))
                .build();
    }
}
