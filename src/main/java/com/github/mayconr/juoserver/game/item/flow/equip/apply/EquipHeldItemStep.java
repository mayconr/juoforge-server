package com.github.mayconr.juoserver.game.item.flow.equip.apply;

import com.github.mayconr.juoserver.game.item.flow.equip.EquipHeldItemContext;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Equips the held item on the player through the mobile equip flow. */
public final class EquipHeldItemStep extends AbstractFlowStep<EquipHeldItemContext> {
    private final MobileModule mobiles;

    public EquipHeldItemStep(MobileModule mobiles) {
        super("equip-held-item");
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(EquipHeldItemContext context) {
        if (mobiles.equipItem(context.getPlayer(), context.getItem())) {
            return StepResult.success();
        }
        return StepResult.failure("EQUIP_FAILED", "Item " + context.getItem().getSerialId() + " was not equipped");
    }
}
