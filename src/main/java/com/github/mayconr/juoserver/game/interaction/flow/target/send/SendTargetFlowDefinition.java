package com.github.mayconr.juoserver.game.interaction.flow.target.send;

import com.github.mayconr.juoserver.game.interaction.flow.target.send.state.SendTargetStep;
import com.github.mayconr.juoserver.game.interaction.flow.target.send.validation.ValidateTargetStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class SendTargetFlowDefinition {
    private SendTargetFlowDefinition() {
    }

    public static Flow<SendTargetContext> build(FlowRegistryFactory.GameModules modules, FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<SendTargetContext>builder()
                .step(new ValidateTargetStep())
                .step(new SendTargetStep(infra.eventBus()))
                .build();
    }
}
