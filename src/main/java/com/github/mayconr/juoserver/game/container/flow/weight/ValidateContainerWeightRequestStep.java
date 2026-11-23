package com.github.mayconr.juoserver.game.container.flow.weight;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ValidateContainerWeightRequestStep extends AbstractFlowStep<ContainerWeightContext> {
    public static final String INVALID_REQUEST = "INVALID_CONTAINER_WEIGHT_REQUEST";

    public ValidateContainerWeightRequestStep() {
        super("validate-container-weight-request");
    }

    @Override
    public StepResult execute(ContainerWeightContext context) {
        if (context.getContainer() == null) {
            return StepResult.failure(INVALID_REQUEST, "Container is required");
        }
        return StepResult.success();
    }
}
