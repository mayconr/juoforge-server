package com.github.mayconr.juoserver.game.ui.flow.status;

import com.github.mayconr.juoserver.game.model.event.StatusGumpRequested;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class SendStatusGumpStep extends AbstractFlowStep<StatusGumpContext> {
    private final EventBus events;
    private final RealmStorage storage;

    public SendStatusGumpStep(EventBus events, RealmStorage storage) {
        super("send-status-gump");
        this.events = events;
        this.storage = storage;
    }

    @Override
    public StepResult execute(StatusGumpContext context) {
        var mobile = storage.getMobile(context.getMobileSerial()).orElse(null);
        if (mobile == null) {
            return StepResult.failure("MOBILE_NOT_FOUND", "Mobile not found " + context.getMobileSerial());
        }
        events.publish(new StatusGumpRequested(context.getPlayer(), mobile));
        return StepResult.success();
    }
}
