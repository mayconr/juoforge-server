package com.github.mayconr.juoserver.game.item.flow.drop.dropitem;

import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext;
import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext.TargetContainer;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResolveMobileTargetStep extends AbstractFlowStep<DropItemContext> {

    private final RealmStorage storage;

    public ResolveMobileTargetStep(RealmStorage storage) {
        super("resolve_mobile_target");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        int serial = context.getDropItem().getContainerSerialId();

        var backpackOpt = storage.getMobile(serial)
                .map(UOMobile::getBackpack)
                .flatMap(storage::getContainer);

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
