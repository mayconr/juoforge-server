package com.github.mayconr.juoserver.game.item.flow.drop.dropitem;

import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveDropTypeStep extends AbstractFlowStep<DropItemContext> {
    public ResolveDropTypeStep() {
        super("resolve_drop_type");
    }

    @Override
    public StepResult execute(DropItemContext context) {
        var dropItem = context.getDropItem();
        int targetSerial = dropItem.getContainerSerialId();

        if (dropItem.isGroundDrop()) {
            context.setTarget(DropItemContext.DropTarget.GROUND);
            return StepResult.success();
        }

        if (UOMobile.isMobile(targetSerial)) {
            context.setTarget(DropItemContext.DropTarget.MOBILE);
            return StepResult.success();
        }

        if (UOItem.isItem(targetSerial)) {
            context.setTarget(DropItemContext.DropTarget.ITEM);
            return StepResult.success();
        }

        return StepResult.failure("Invalid drop type.");
    }
}
