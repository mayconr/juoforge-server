package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Successful ground drops and stack interactions renew the destination's drop date. */
public final class RecordGroundDropStep extends AbstractFlowStep<DropItemContext> {
    private final ItemModule items;

    public RecordGroundDropStep(ItemModule items) {
        super("record-ground-drop");
        this.items = items;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        if (context.isGroundDrop()) {
            items.recordGroundInteraction(context.getItem());
        } else if (context.isStack() && context.getTargetStack().itemStack().isOnTheGround()) {
            items.recordGroundInteraction(context.getTargetStack().itemStack());
        }
        return StepResult.success();
    }
}
