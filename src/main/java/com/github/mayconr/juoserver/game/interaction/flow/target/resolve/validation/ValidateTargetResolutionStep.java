package com.github.mayconr.juoserver.game.interaction.flow.target.resolve.validation;

import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateTargetResolutionStep extends AbstractFlowStep<ResolveTargetContext> {
    public ValidateTargetResolutionStep() {
        super("ValidateTargetResolution");
    }

    @Override
    public StepResult execute(ResolveTargetContext context) {
        final var player = context.getPlayer();
        final var target = context.getTarget();

        if (player == null) {
            return StepResult.failure("Mobile is null");
        }
        if (target == null) {
            return StepResult.failure("Target is null");
        }

        final var attributes = player.runtimeAttributes();
        if (attributes == null) {
            return StepResult.failure("Mobile runtime attributes is null");
        }

        if (!attributes.contains("TARGET_" + target.getCursorId())) {
            return StepResult.failure("Target " + target.getCursorId() + " doesn't exist");
        }

        return StepResult.success();
    }
}
