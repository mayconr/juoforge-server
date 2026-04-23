package com.github.mayconr.juoserver.game.mobile.flow.equip;

import com.github.mayconr.juoserver.game.flow.EquipItemFlowDefinition.EquipItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateItemEquipStep extends AbstractFlowStep<EquipItemContext> {
    public ValidateItemEquipStep(int order) {
        super("validate_item_equip", order, FlowPhase.CORE);
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        if (context.getItem() == null) {
            return StepResult.failure("item is null");
        }
        return StepResult.success();
    }
}
