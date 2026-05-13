package com.github.mayconr.juoserver.game.item.flow.creation.validation;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
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
