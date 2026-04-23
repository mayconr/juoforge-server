package com.github.mayconr.juoserver.game.item.flow.creation;

import com.github.mayconr.juoserver.game.flow.ItemCreationFlowDefinition.ItemCreationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateItemCreation extends AbstractFlowStep<ItemCreationContext> {
    public ValidateItemCreation() {
        super("validate-item-creation");
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        return StepResult.success();
    }
}
