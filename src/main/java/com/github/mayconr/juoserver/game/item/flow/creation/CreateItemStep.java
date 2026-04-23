package com.github.mayconr.juoserver.game.item.flow.creation;

import com.github.mayconr.juoserver.game.flow.ItemCreationFlowDefinition.ItemCreationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateItemStep extends AbstractFlowStep<ItemCreationContext> {

    private final RealmStorage storage;

    public CreateItemStep(RealmStorage storage) {
        super("create_item");
        this.storage = storage;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        var data = context.getData();
        if (data == null) {
            return StepResult.failure("data is null");
        }

        var item = storage.createItem(data);

        context.complete(item);

        return StepResult.success();
    }
}
