package com.github.mayconr.juoserver.game.mobile.flow.equip.apply;

import com.github.mayconr.juoserver.game.item.ItemPlacement;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class EquipItemStep extends AbstractFlowStep<EquipItemContext> {

    private final WorldMobile mobiles;

    public EquipItemStep(WorldMobile mobiles) {
        super("equip_item");
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        final var mobile = context.getMobile();
        final var item = context.getItem();

        ItemPlacement.equip(mobile, item, mobiles);
        context.setEquipped(true);

        return StepResult.success();
    }
}
