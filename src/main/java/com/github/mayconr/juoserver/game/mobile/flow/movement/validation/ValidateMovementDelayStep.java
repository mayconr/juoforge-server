package com.github.mayconr.juoserver.game.mobile.flow.movement.validation;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext.MoveIntent;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateMovementDelayStep extends AbstractFlowStep<MovementContext> {

    public static final String LAST_MOVE_TIME = "MOVEMENT.LAST_MOVE_TIME";
    public static final String LAST_MOVE_INTENT = "MOVEMENT.LAST_MOVE_INTENT";
    private static final int MOUNTED_CADENCE = 80;//ms
    private static final int UNMOUNTED_CADENCE = 180;//ms

    private final EventBus eventBus;

    public ValidateMovementDelayStep(EventBus eventBus) {
        super("ValidateMovementDelay");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(MovementContext context) {
        final var mobile = context.getMobile();
        final var running = context.isRunning();
        final long now = System.currentTimeMillis();
        final var attributes = mobile.runtimeAttributes();
        final long lastMove = attributes.getOrDefault(LAST_MOVE_TIME, 0L);
        final MoveIntent lastMoveIntent = attributes.getOrDefault(LAST_MOVE_INTENT, MoveIntent.FORWARD);

        final long elapsed = now - lastMove;
        long requiredDelay = calculateMoveDelay(mobile, running, lastMoveIntent);
        long tolerance = calculateTolerance(requiredDelay);
        long minimumAcceptedDelay = requiredDelay - tolerance;

        if (elapsed < minimumAcceptedDelay) {
            return StepResult.failure("Movement delay has been reached. elapsed "+elapsed +" accepted "+minimumAcceptedDelay +" for player " + mobile.getName());
        }

        attributes.set(LAST_MOVE_TIME, now);
        attributes.set(LAST_MOVE_INTENT, context.getMoveIntent());

        return StepResult.success();
    }

    private long calculateMoveDelay(UOMobile mobile, boolean running, MoveIntent moveIntent) {
        if (mobile.isMounted() || moveIntent == MoveIntent.TURN) {
            return MOUNTED_CADENCE + (running ? 0 : 30);
        }

        return UNMOUNTED_CADENCE + (running ? 0 : 30);
    }

    private long calculateTolerance(long delay) {
        return Math.min(30, delay / 5);
    }
}
