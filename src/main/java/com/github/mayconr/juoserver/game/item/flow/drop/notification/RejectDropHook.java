package com.github.mayconr.juoserver.game.item.flow.drop.notification;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.item.hold.RestoreHeldItem;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.game.model.event.ItemDropRejected;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.HookStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Restores the actual held item on any client drop failure, including an invalid packet serial. */
public final class RejectDropHook implements HookStep<DropItemContext> {
    private final HeldItems heldItems;
    private final RestoreHeldItem restorer;
    private final EventBus events;

    public RejectDropHook(HeldItems heldItems, RestoreHeldItem restorer, EventBus events) {
        this.heldItems = heldItems;
        this.restorer = restorer;
        this.events = events;
    }

    @Override
    public void execute(String stepName, StepResult result, DropItemContext context) {
        if (!context.isDroppedItem() || context.isOutcomeNotified()) {
            return;
        }
        var held = heldItems.get(context.getPlayer());
        context.setOutcomeNotified(true);
        try {
            held.ifPresent(item -> restorer.restore(item, context.getPlayer()));
        } finally {
            heldItems.release(context.getPlayer());
            events.publish(new ItemDropRejected(
                    context.getPlayer(),
                    reason(result),
                    held.map(item -> item.item()).orElse(null)
            ));
        }
    }

    private static LiftRejectReason reason(StepResult result) {
        if (result == null) {
            return LiftRejectReason.INSPECIFIC;
        }
        return switch (result.code() == null ? "" : result.code()) {
            case "DROP_OUT_OF_RANGE" -> LiftRejectReason.OUT_OF_RANGE;
            case "DROP_ACCESS_DENIED" -> LiftRejectReason.TRY_TO_STEAL;
            default -> LiftRejectReason.INSPECIFIC;
        };
    }
}
