package com.github.mayconr.juoserver.game.mobile.movement;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.MovementResult;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MovementService {

    private final EventBus eventBus;
    private final MobileMovementRules movementRules;

    public void move(UOMobile mobile, MoveRequest request) {
        if (request == null) {
            return;
        }

        final MovementResult result = movementRules.tryMove(mobile, request.getDirection(), request.isRunning());

        applyMove(mobile, result, request.getSequence(), false);
    }

    public void move(UOMobile mobile, Location location) {
        final MovementResult result =
                movementRules.tryMove(mobile, location);

        applyMove(mobile, result, 0, true);
    }

    public void move(UOMobile mobile, Direction direction) {
        final MovementResult result =
                movementRules.tryMove(mobile, direction, false);

        applyMove(mobile, result, 0, false);
    }

    private void applyMove(UOMobile mobile,
                           MovementResult result,
                           int sequence,
                           boolean teleport) {
        if (!result.success()) {
            return;
        }
        movementRules.applyMove(mobile, result);

        eventBus.publish(new MobileMoved(mobile, result, sequence, teleport));
    }
}
