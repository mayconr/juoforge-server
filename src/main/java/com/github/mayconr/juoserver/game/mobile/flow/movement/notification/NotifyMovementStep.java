package com.github.mayconr.juoserver.game.mobile.flow.movement.notification;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class NotifyMovementStep extends AbstractFlowStep<MovementContext> {
    private final EventBus events;

    public NotifyMovementStep(EventBus events) {
        super("NotifyMovement");
        this.events = events;
    }

    @Override
    public StepResult execute(MovementContext context) {
        events.publish(new MobileMoved(context.getMobile(), context.getDirection(), context.getFrom(),
                context.getTargetLocation(), context.isRunning(), context.getSequence(), false));
        return StepResult.success();
    }
}
