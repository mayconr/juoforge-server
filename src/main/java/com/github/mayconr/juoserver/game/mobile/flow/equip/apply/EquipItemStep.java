package com.github.mayconr.juoserver.game.mobile.flow.equip.apply;

import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class EquipItemStep extends AbstractFlowStep<EquipItemContext> {
    public EquipItemStep() {
        super("equip_item");
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
