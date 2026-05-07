package com.github.mayconr.juoserver.game.mobile.flow.unequip.validation;

import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemContext;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateUnequipItemStep extends AbstractFlowStep<UnequipItemContext> {
    public ValidateUnequipItemStep() {
        super("validate_unequip_item");
    }

    @Override
    public StepResult execute(UnequipItemContext context) {
        final var player = context.getMobile();
        final var item = context.getItem();

        if (context.getItem() == null) {
            return StepResult.failure("item is null");
        }

        if (!item.isMovable()) {
            return StepResult.failure("item is not movable");
        }

        if (!item.getFlags().contains(ItemFlag.WEARABLE)) {
            return StepResult.failure("item is not wearable");
        }

        if (!player.isItemEquipped(item)) {
            return StepResult.failure("item is not equipped");
        }

        return StepResult.success();
    }
}
