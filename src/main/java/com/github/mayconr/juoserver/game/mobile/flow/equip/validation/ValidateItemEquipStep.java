package com.github.mayconr.juoserver.game.mobile.flow.equip.validation;

import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateItemEquipStep extends AbstractFlowStep<EquipItemContext> {
    public ValidateItemEquipStep() {
        super("validate_item_equip");
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        if (context.getItem() == null) {
            return StepResult.failure("item is null");
        }
        var item = context.getItem();
        if (item.getLayer() == null) {
            return StepResult.failure("item has no equipment layer");
        }
        var mobile = context.getMobile();
        if (!mobile.isLayerAvailable(item.getLayer()) && !mobile.isItemEquipped(item)) {
            return StepResult.failure("equipment layer is occupied");
        }
        return StepResult.success();
    }
}
