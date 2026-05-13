package com.github.mayconr.juoserver.game.mobile.flow.equip.validation;

import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
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
        return StepResult.success();
    }
}
