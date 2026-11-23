package com.github.mayconr.juoserver.game.container.flow.open;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

final class EquipContainerBeforeOpeningStep extends AbstractFlowStep<OpenContainerContext> {
    private final ItemModule items;

    EquipContainerBeforeOpeningStep(ItemModule items) {
        super("equip-container-before-opening");
        this.items = items;
    }

    @Override
    public StepResult execute(OpenContainerContext context) {
        var player = context.getPlayer();
        synchronized (player) {
            if (context.getContainer() instanceof UOItem item && item.getLayer() != null) {
                if (ItemLocation.equipped(player.getSerialId()).equals(item.getCurrentLocation())
                        && player.isItemEquipped(item)) {
                    return StepResult.success();
                }
                if (item.getCurrentLocation() instanceof OrphanLocation && items.equipItem(player, item, false)) {
                    return StepResult.success();
                }
            }
        }
        return StepResult.failure("CONTAINER_EQUIP_FAILED", "Cannot equip container before opening");
    }
}
