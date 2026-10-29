package com.github.mayconr.juoserver.game.ui.flow.status;

import com.github.mayconr.juoserver.game.model.event.StatusGumpRequested;
import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class SendStatusGumpStep extends AbstractFlowStep<StatusGumpContext> {
    private final EventBus events;
    private final RealmStorage storage;
    private final WorldContainer containers;

    public SendStatusGumpStep(EventBus events, RealmStorage storage, WorldContainer containers) {
        super("send-status-gump");
        this.events = events;
        this.storage = storage;
        this.containers = containers;
    }

    @Override
    public StepResult execute(StatusGumpContext context) {
        var mobile = storage.getMobile(context.getMobileSerial()).orElse(null);
        if (mobile == null) {
            return StepResult.failure("MOBILE_NOT_FOUND", "Mobile not found " + context.getMobileSerial());
        }
        long weight = 0;
        if (mobile.getBackpack() != null) {
            var backpack = storage.getContainer(mobile.getBackpack()).orElse(null);
            if (backpack == null) {
                return StepResult.failure("BACKPACK_NOT_FOUND",
                        "Cannot calculate status weight: backpack " + mobile.getBackpack() + " is not loaded");
            }
            weight = containers.getWeight(backpack);
        }
        events.publish(new StatusGumpRequested(context.getPlayer(), mobile, weight));
        return StepResult.success();
    }
}
