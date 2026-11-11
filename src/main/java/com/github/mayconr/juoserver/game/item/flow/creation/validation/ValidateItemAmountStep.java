package com.github.mayconr.juoserver.game.item.flow.creation.validation;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Rejects amounts above one for graphics the client tiledata does not mark as stackable. */
public class ValidateItemAmountStep extends AbstractFlowStep<ItemCreationContext> {

    public ValidateItemAmountStep() {
        super("validate-item-amount");
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        int amount = context.getRequest().amount();
        if (amount > 1 && !context.isStackable()) {
            return StepResult.failure("ITEM_NOT_STACKABLE",
                    "Item " + context.getTemplate().name() + " is not stackable and cannot be created with amount "
                            + amount,
                    "{item.not_stackable}");
        }
        return StepResult.success();
    }
}
