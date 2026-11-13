package com.github.mayconr.juoserver.game.item.flow.update;

import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class ApplyItemChangeStep extends AbstractFlowStep<UpdateItemContext> {
    private final WorldStorage storage;
    private final EventBus events;

    public ApplyItemChangeStep(WorldStorage storage, EventBus events) {
        super("apply-item-change");
        this.storage = storage;
        this.events = events;
    }

    @Override
    public StepResult execute(UpdateItemContext context) {
        var item = context.getItem();
        var change = context.getChange();
        synchronized (item) {
            if (change.location() == null && change.modelId() == null) {
                return StepResult.success();
            }
            if (!item.isOnTheGround()) {
                return StepResult.failure("Only ground items can be updated");
            }
            var previous = new PointInTheWorld(item);
            boolean moved = change.location() != null && !previous.equals(change.location());
            boolean remodeled = change.modelId() != null && change.modelId() != item.getModelId();
            if (!moved && !remodeled) {
                return StepResult.success();
            }
            if (moved) {
                storage.items().removeFromGround(item);
                item.setLocation(change.location());
                storage.items().placeOnGround(item);
            }
            if (remodeled) {
                item.setModelId(change.modelId());
            }
            events.publish(new ItemUpdated(item, null, previous));
            return StepResult.success();
        }
    }
}
