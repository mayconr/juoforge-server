package com.github.mayconr.juoserver.game.container.flow.count;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Sums the collected items whose internal name matches, by stack amount or one per item. */
public final class SumMatchingItemsStep extends AbstractFlowStep<CountContainerItemsContext> {
    public SumMatchingItemsStep() {
        super("sum-matching-items");
    }

    @Override
    public StepResult execute(CountContainerItemsContext context) {
        int total = 0;
        for (var item : context.getItems()) {
            if (context.getName().equals(item.getName())) {
                total = Math.addExact(total, quantity(context, item));
            }
        }
        context.complete(total);
        return StepResult.success();
    }

    private int quantity(CountContainerItemsContext context, UOItem item) {
        return context.getOptions().countStackAmount() ? item.getAmount() : 1;
    }
}
