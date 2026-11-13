package com.github.mayconr.juoserver.game.item.flow.drop.dropitem;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResolveItemTargetStep extends AbstractFlowStep<DropItemContext> {

    private final WorldStorage storage;

    public ResolveItemTargetStep(WorldStorage storage) {
        super("resolve_item_target");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        int serial = context.getDropItem().getContainerSerialId();

        var targetOpt = storage.items().get(serial);
        if (targetOpt.isEmpty()) {
            return StepResult.failure("Container not found for serial " + serial);
        }

        var target = targetOpt.get();

        if (target instanceof UOContainer container) {
            var dropItem = context.getDropItem();
            var location = new PointInTheWorld(
                    dropItem.getX(),
                    dropItem.getY(),
                    dropItem.getContainerGridIndex()
            );

            context.setTargetContainer(new DropItemContext.TargetContainer(container, location));
        } else {
            context.setTargetStack(new DropItemContext.TargetStack(target));
        }

        return StepResult.success();
    }
}
