package com.github.mayconr.juoserver.game.container.flow.query;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.ArrayList;

public final class FilterContainerItemsStep extends AbstractFlowStep<GetContainerItemsContext> {
    public FilterContainerItemsStep() {
        super("filter-container-items");
    }

    @Override
    public StepResult execute(GetContainerItemsContext context) {
        var items = new ArrayList<UOItem>();
        for (var item : context.getItems()) {
            if (context.getPredicate().test(item)) {
                items.add(item);
            }
        }
        context.complete(items);
        return StepResult.success();
    }
}
