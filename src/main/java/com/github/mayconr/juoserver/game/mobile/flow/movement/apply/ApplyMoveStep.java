package com.github.mayconr.juoserver.game.mobile.flow.movement.apply;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class ApplyMoveStep extends AbstractFlowStep<MovementContext> {

    private final EventBus eventBus;
    private final RealmStorage storage;

    public ApplyMoveStep(EventBus eventBus,  RealmStorage storage) {
        super("ApplyMoveStep");
        this.eventBus = eventBus;
        this.storage = storage;
    }

    @Override
    public StepResult execute(MovementContext context) {
        final var mobile = context.getMobile();
        final var direction = context.getDirection();
        final var from = mobile;
        final var to = context.getTargetLocation();
        final var running = context.isRunning();
        final var sequence = context.getSequence();

        synchronized (this) {
            mobile.setDirection(direction);
            mobile.setRunning(running);
            mobile.setLocation(to);
            storage.updateMobileLocation(mobile, from, to);
        }

        eventBus.publish(new MobileMoved(mobile, direction, from, to, running, sequence, false));
        return StepResult.success();
    }
}
