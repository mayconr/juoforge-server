package com.github.mayconr.juoserver.game.mobile.flow.teleport.notification;

import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportContext;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class NotifyTeleportStep extends AbstractFlowStep<TeleportContext> {
    private final EventBus events;

    public NotifyTeleportStep(EventBus events) {
        super("NotifyTeleport");
        this.events = events;
    }

    @Override
    public StepResult execute(TeleportContext context) {
        events.publish(new MobileMoved(context.getMobile(), context.getDirection(),
                context.getFrom(), context.getLocation(), false, 0, true));
        return StepResult.success();
    }
}
