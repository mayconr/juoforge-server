package com.github.mayconr.juoserver.game.mobile.flow.equip;

import com.github.mayconr.juoserver.game.flow.EquipItemFlowDefinition.EquipItemContext;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class EquipItemStep extends AbstractFlowStep<EquipItemContext> {
    public EquipItemStep(int order) {
        super("equip_item", order, FlowPhase.CORE);
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        final var mobile = context.getMobile();
        final var item = context.getItem();

        item.setCurrentLocation(new EquippedLocation(mobile.getSerialId()));
        mobile.equipItem(item);

        return StepResult.success();
    }
}
