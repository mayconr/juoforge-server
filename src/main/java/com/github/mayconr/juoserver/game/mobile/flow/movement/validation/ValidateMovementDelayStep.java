package com.github.mayconr.juoserver.game.mobile.flow.movement.validation;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext.MoveIntent;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateMovementDelayStep extends AbstractFlowStep<MovementContext> {

    public static final String LAST_MOVE_TIME = "MOVEMENT.LAST_MOVE_TIME";
    public static final String LAST_MOVE_INTENT = "MOVEMENT.LAST_MOVE_INTENT";
    private static final int MOUNTED_CADENCE = 80;//ms
    private static final int UNMOUNTED_CADENCE = 180;//ms
    private static final int MAX_MOVEMENT_DEXTERITY = 100;
    private static final int MAX_WALKING_DEXTERITY = 40;
    private static final long NPC_DELAY_SCALE_MILLIS = 180;

    public ValidateMovementDelayStep() {
        super("ValidateMovementDelay");
    }

    @Override
    public StepResult execute(MovementContext context) {
        final var mobile = context.getMobile();
        if (mobile instanceof UONpc && mobile.getDexterity() <= 0) {
            return StepResult.failure("NPC cannot move with zero or negative dexterity");
        }

        final var running = context.isRunning();
        final long now = System.currentTimeMillis();
        final var attributes = mobile.runtimeAttributes();
        final long lastMove = attributes.getOrDefault(LAST_MOVE_TIME, 0L);
        final MoveIntent lastMoveIntent = attributes.getOrDefault(LAST_MOVE_INTENT, MoveIntent.FORWARD);

        final long elapsed = now - lastMove;
        long minimumAcceptedDelay = minimumAcceptedDelay(mobile, running, lastMoveIntent);

        if (elapsed < minimumAcceptedDelay) {
            return StepResult.failure("Movement delay has been reached. elapsed "+elapsed +" accepted "+minimumAcceptedDelay +" for player " + mobile.getName());
        }

        if (mobile instanceof UONpc) {
            context.setRunning(mobile.getDexterity() > MAX_WALKING_DEXTERITY);
        }

        attributes.set(LAST_MOVE_TIME, now);
        attributes.set(LAST_MOVE_INTENT, context.getMoveIntent());

        return StepResult.success();
    }

    private long minimumAcceptedDelay(UOMobile mobile, boolean running, MoveIntent lastMoveIntent) {
        if (mobile instanceof UONpc npc) {
            int dexterity = Math.clamp(npc.getDexterity(), 1, MAX_MOVEMENT_DEXTERITY);
            return Math.ceilDiv(NPC_DELAY_SCALE_MILLIS * (MAX_MOVEMENT_DEXTERITY - dexterity), dexterity);
        }

        long delay = calculateMoveDelay(mobile, running, lastMoveIntent);
        return delay - calculateTolerance(delay);
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
