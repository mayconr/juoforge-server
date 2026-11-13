package com.github.mayconr.juoserver.game.item.flow.drop.notification;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.model.event.ItemDropAccepted;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Confirms a client drop only after all placement steps have succeeded. */
public final class AcceptDropStep extends AbstractFlowStep<DropItemContext> {
    private final HeldItems heldItems;
    private final EventBus events;

    public AcceptDropStep(HeldItems heldItems, EventBus events) {
        super("accept-drop");
        this.heldItems = heldItems;
        this.events = events;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        heldItems.release(context.getPlayer());
        context.setOutcomeNotified(true);
        events.publish(new ItemDropAccepted(context.getPlayer()));
        return StepResult.success();
    }
}
