package com.github.mayconr.juoserver.game.item.flow.lift.notification;

import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.model.event.ItemLiftRejected;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.HookStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/**
 * Runs when any lift step fails, including by an exception: frees the cursor if the item was already held and
 * tells the client to put the item back.
 */
public final class RejectLiftHook implements HookStep<LiftItemContext> {
    private final HeldItems heldItems;
    private final EventBus eventBus;

    public RejectLiftHook(HeldItems heldItems, EventBus eventBus) {
        this.heldItems = heldItems;
        this.eventBus = eventBus;
    }

    @Override
    public void execute(String stepName, StepResult result, LiftItemContext context) {
        if (context.isHeld()) {
            heldItems.release(context.getPlayer());
            context.setHeld(false);
        }
        eventBus.publish(new ItemLiftRejected(context.getPlayer(), context.getRejection()));
    }
}
