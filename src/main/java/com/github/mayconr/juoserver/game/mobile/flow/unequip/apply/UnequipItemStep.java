package com.github.mayconr.juoserver.game.mobile.flow.unequip.apply;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemPlacement;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemContext;
import com.github.mayconr.juoserver.game.model.GroundItemTarget;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedOnTheGround;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;

public class UnequipItemStep extends AbstractFlowStep<UnequipItemContext> {
    private final GameInfra infra;
    private final WorldMobile mobiles;
    private final ItemModule items;

    public UnequipItemStep(GameInfra infra, WorldMobile mobiles, ItemModule items) {
        super("unequip_item");
        this.infra = infra;
        this.mobiles = mobiles;
        this.items = items;
    }

    @Override
    public StepResult execute(UnequipItemContext context) {
        var mobile = context.getMobile();
        var item = context.getItem();
        if (context.getTarget() instanceof GroundItemTarget ground) {
            ItemPlacement.placeOnGround(infra.storage(), item, ground.location());
            ItemPlacement.removeEquipment(mobile, item, mobiles);
            infra.eventBus().publish(new ItemDroppedOnTheGround(mobile, item, ground.location()));
            items.recordGroundInteraction(item);
        } else {
            ItemPlacement.removeEquipment(mobile, item, mobiles);
            item.setCurrentLocation(ItemLocation.orphan());
        }
        context.setUnequipped(true);
        return StepResult.success();
    }
}
