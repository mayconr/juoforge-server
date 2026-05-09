package com.github.mayconr.juoserver.game.interaction.flow.target.resolve;

import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.build.BuildTargetResultStep;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.build.ExtractTargetMetadataStep;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.dispatch.DispatchTargetResultStep;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.resolution.ResolveTargetEntityStep;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.validation.ValidateTargetResolutionStep;
import com.github.mayconr.juoserver.game.shared.step.ValidateLightOfSightStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class ResolveTargetFlowDefinition {
    private ResolveTargetFlowDefinition() {
    }

    public static Flow<ResolveTargetContext> build(FlowRegistryFactory.GameModules modules, FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<ResolveTargetContext>builder()
                .step(new ValidateTargetResolutionStep())
                .step(new ExtractTargetMetadataStep())
                .step(new ResolveTargetEntityStep(infra.storage()))
                .step(new BuildTargetResultStep(infra.fileReader()))
                .step(new ValidateLightOfSightStep<>(infra.fileReader(), modules.message()), ResolveTargetContext::isValidateLOS)
                .step(new DispatchTargetResultStep())
                .build();
    }
}
