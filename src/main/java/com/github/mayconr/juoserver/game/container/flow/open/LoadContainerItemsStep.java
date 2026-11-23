package com.github.mayconr.juoserver.game.container.flow.open;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;

final class LoadContainerItemsStep extends AbstractFlowStep<OpenContainerContext> {
    private final Items items;

    LoadContainerItemsStep(Items items) {
        super("load-container-items");
        this.items = items;
    }

    @Override
    public StepResult execute(OpenContainerContext context) {
        context.setContents(items.loadContainerItems(context.getContainer()));
        return StepResult.success();
    }
}
