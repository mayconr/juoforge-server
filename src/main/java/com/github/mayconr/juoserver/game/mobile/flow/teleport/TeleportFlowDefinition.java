package com.github.mayconr.juoserver.game.mobile.flow.teleport;

import com.github.mayconr.juoserver.game.mobile.flow.teleport.relocation.ApplyTeleport;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.resolver.ResolveDirectionStep;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.validation.ValidateDestinationStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class TeleportFlowDefinition {
    private TeleportFlowDefinition() {
    }

    public static Flow<TeleportContext> build(FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<TeleportContext>builder()
                .step(new ValidateDestinationStep(infra.fileReader()))
                .step(new ResolveDirectionStep())
                .step(new ApplyTeleport(infra.eventBus(), infra.storage()))
                .build();
    }
}
