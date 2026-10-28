package com.github.mayconr.juoserver.game.mobile.flow.movement.apply;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class ApplyMoveStep extends AbstractFlowStep<MovementContext> {

    private final RealmStorage storage;

    public ApplyMoveStep(RealmStorage storage) {
        super("ApplyMoveStep");
        this.storage = storage;
    }

    @Override
    public StepResult execute(MovementContext context) {
        final var mobile = context.getMobile();
        final var direction = context.getDirection();
        final var from = new PointInTheWorld(mobile);
        context.setFrom(from);
        final var to = context.getTargetLocation();
        final var running = context.isRunning();

        synchronized (this) {
            mobile.setDirection(direction);
            mobile.setRunning(running);
            mobile.setLocation(to);
            storage.updateMobileLocation(mobile, from, to);
        }

        return StepResult.success();
    }
}
