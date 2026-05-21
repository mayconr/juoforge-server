package com.github.mayconr.juoserver.game.mobile.flow.teleport.relocation;

import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportContext;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class ApplyTeleport extends AbstractFlowStep<TeleportContext> {

    private final EventBus eventBus;
    private final RealmStorage storage;

    public ApplyTeleport(EventBus eventBus, RealmStorage storage) {
        super("ApplyTeleport");
        this.eventBus = eventBus;
        this.storage = storage;
    }

    @Override
    public StepResult execute(TeleportContext context) {
        final var mobile = context.getMobile();
        final var to = context.getLocation();
        final var direction = context.getDirection();

        synchronized (this) {
            mobile.setDirection(direction);
            mobile.setRunning(false);
            mobile.setLocation(to);
            storage.updateMobileLocation(mobile, mobile, to);
        }

        eventBus.publish(new MobileMoved(mobile, direction, mobile, to, false, 0, true));
        return StepResult.success();
    }
}
