package com.github.mayconr.juoserver.game.mobile.flow.resync;

import com.github.mayconr.juoserver.game.mobile.flow.resync.resolver.ResolveResyncSequenceStep;
import com.github.mayconr.juoserver.game.mobile.flow.resync.validation.ValidateResyncRequestStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class ResyncFlowDefinition {
    private ResyncFlowDefinition() {
    }

    public static Flow<ResyncContext> build(FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<ResyncContext>builder()
                .step(new ValidateResyncRequestStep())
                .step(new ResolveResyncSequenceStep(infra.eventBus()))
                .build();
    }
}
