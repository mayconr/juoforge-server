package com.github.mayconr.juoserver.game.mobile.flow.movement;

import com.github.mayconr.juoserver.game.mobile.flow.movement.apply.ApplyMoveStep;
import com.github.mayconr.juoserver.game.mobile.flow.movement.apply.UpdateSequenceStep;
import com.github.mayconr.juoserver.game.mobile.flow.movement.hook.MovementFailureHook;
import com.github.mayconr.juoserver.game.mobile.flow.movement.resolver.ResolveMoveIntentStep;
import com.github.mayconr.juoserver.game.mobile.flow.movement.resolver.ResolveTargetLocationStep;
import com.github.mayconr.juoserver.game.mobile.flow.movement.validation.ValidateMovementParamsStep;
import com.github.mayconr.juoserver.game.mobile.flow.movement.validation.ValidateSequenceStep;
import com.github.mayconr.juoserver.game.mobile.flow.movement.validation.ValidateMovementDelayStep;
import com.github.mayconr.juoserver.game.mobile.flow.movement.validation.ValidateTargetTileStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class MovementFlowDefinition {
    private MovementFlowDefinition() {
    }

    public static Flow<MovementContext> build(FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<MovementContext>builder()
                .step(new ValidateMovementParamsStep())
                // Identify movement intent
                .step(new ResolveMoveIntentStep())
                // Validate the received sequence match with the expected
                .step(new ValidateSequenceStep(), MovementContext::isRequested)
                // Validate delay between move requests
                .step(new ValidateMovementDelayStep(infra.eventBus()))
                // Resolve the x,y,z destination of the movement
                .step(new ResolveTargetLocationStep())
                // Check if the target is passable
                .step(new ValidateTargetTileStep(infra.fileReader()))
                // Update the sequence
                .step(new UpdateSequenceStep())
                // Apply the movement
                .step(new ApplyMoveStep(infra.eventBus(), infra.storage()))

                // Hooks
                .hook(new MovementFailureHook(infra.eventBus()))
                .build();
    }
}
