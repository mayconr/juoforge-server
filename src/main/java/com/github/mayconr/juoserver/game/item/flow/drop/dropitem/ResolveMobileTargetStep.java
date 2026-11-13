package com.github.mayconr.juoserver.game.item.flow.drop.dropitem;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext.TargetContainer;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResolveMobileTargetStep extends AbstractFlowStep<DropItemContext> {

    private final WorldStorage storage;

    public ResolveMobileTargetStep(WorldStorage storage) {
        super("resolve_mobile_target");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        int serial = context.getDropItem().getContainerSerialId();

        var backpackOpt = storage.mobiles().get(serial)
                .map(UOMobile::getBackpack)
                .flatMap(storage.items()::getContainer);

        if (backpackOpt.isEmpty()) {
            return StepResult.failure("Backpack not found for mobile serial " + serial);
        }

        var dropItem = context.getDropItem();
        var location = new PointInTheWorld(
                dropItem.getX(),
                dropItem.getY(),
                dropItem.getContainerGridIndex()
        );

        context.setTargetContainer(new TargetContainer(backpackOpt.get(), location));
        return StepResult.success();
    }
}
