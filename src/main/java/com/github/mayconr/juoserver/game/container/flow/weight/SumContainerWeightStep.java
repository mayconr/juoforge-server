package com.github.mayconr.juoserver.game.container.flow.weight;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Sums the weight of the container itself and of all collected contents. */
public final class SumContainerWeightStep extends AbstractFlowStep<ContainerWeightContext> {
    public SumContainerWeightStep() {
        super("sum-container-weight");
    }

    @Override
    public StepResult execute(ContainerWeightContext context) {
        long weight = weightOf(context.getContainer());
        for (var item : context.getContents()) {
            weight = Math.addExact(weight, weightOf(item));
        }
        context.complete(weight);
        return StepResult.success();
    }

    private long weightOf(UOItem item) {
        return Math.multiplyExact((long) item.getUnitWeight(), item.getAmount());
    }
}
