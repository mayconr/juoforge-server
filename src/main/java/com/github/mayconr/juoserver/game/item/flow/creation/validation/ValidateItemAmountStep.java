package com.github.mayconr.juoserver.game.item.flow.creation.validation;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import static com.github.mayconr.juoserver.game.item.ItemCreationFailure.NOT_STACKABLE;

/** Rejects amounts above one for graphics the client tiledata does not mark as stackable. */
public class ValidateItemAmountStep extends AbstractFlowStep<ItemCreationContext> {

    public ValidateItemAmountStep() {
        super("validate-item-amount");
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        int amount = context.getRequest().amount();
        if (amount > 1 && !context.isStackable()) {
            return StepResult.failure(NOT_STACKABLE.code(),
                    "Item " + context.getTemplate().name() + " is not stackable and cannot be created with amount "
                            + amount,
                    NOT_STACKABLE.displayMessage());
        }
        return StepResult.success();
    }
}
