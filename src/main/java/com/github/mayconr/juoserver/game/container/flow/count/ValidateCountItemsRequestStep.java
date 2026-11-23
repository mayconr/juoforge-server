package com.github.mayconr.juoserver.game.container.flow.count;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ValidateCountItemsRequestStep extends AbstractFlowStep<CountContainerItemsContext> {
    public static final String INVALID_REQUEST = "INVALID_COUNT_ITEMS_REQUEST";

    public ValidateCountItemsRequestStep() {
        super("validate-count-items-request");
    }

    @Override
    public StepResult execute(CountContainerItemsContext context) {
        if (context.getContainer() == null || context.getName() == null || context.getOptions() == null) {
            return StepResult.failure(INVALID_REQUEST, "Container, item name and options are required");
        }
        return StepResult.success();
    }
}
