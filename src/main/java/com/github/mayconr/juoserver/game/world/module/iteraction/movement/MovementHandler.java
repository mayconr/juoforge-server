package com.github.mayconr.juoserver.game.world.module.iteraction.movement;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.world.MovementInternal;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MovementHandler implements MovementInternal {

    private final EventBus eventBus;
    private final RealmStorage storage;

    @Override
    public void move(UOPlayer player, MoveRequest request) {
        if (request == null) {
            return;
        }

        var result = tryMove(player, request);

        if (!result.success()) {
            // TODO Refuse movement
            return;
        }

        applyMove(player, result);
        eventBus.publish(new MobileMoved(player, result, request.getSequence(), false));
    }

    @Override
    public void move(UOPlayer player, Location location) {
        final var result = tryMove(player, location);
        if (!result.success()) {
            // TODO Refuse movement
            return;
        }

        applyMove(player, result);
        eventBus.publish(new MobileMoved(player, result, 0, true));
    }

    private MovementResult tryMove(UOMobile mobile, MoveRequest request) {
        final var direction = request.getDirection();
        Location to;
        if (mobile.getDirection().equals(direction)) {
            to = new PointInTheWorld(mobile.getX() + direction.getDx(), mobile.getY() + direction.getDy(), mobile.getZ());
        } else {
            to = mobile;
        }
        return MovementResult.success(mobile, direction, to, request.isRunning());
    }

    private MovementResult tryMove(UOMobile mobile, Location location) {
        final int dx = location.getX() - mobile.getX();
        final int dy = location.getY() - mobile.getY();

        if (dx == 0 && dy == 0) {
            return MovementResult.denied(mobile, MovementFailureReason.BLOCKED);
        }

        final Direction direction = Direction.fromDelta(dx, dy);

        return MovementResult.success(
                mobile,
                direction,
                location,
                false
        );
    }

    private void applyMove(UOMobile mobile, MovementResult result) {
        if (result.success()) {
            synchronized (this) {
                mobile.setDirection(result.direction());
                mobile.setRunning(result.running());
                mobile.setLocation(result.to());
                storage.updateMobileLocation(mobile, result.from(), result.to());
            }
        }
    }
}
