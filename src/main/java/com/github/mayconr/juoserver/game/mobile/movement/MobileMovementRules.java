package com.github.mayconr.juoserver.game.mobile.movement;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MobileMovementRules {

    private final RealmStorage storage;
    
    public MovementResult tryMove(UOMobile mobile, Direction direction, boolean running) {
        Location to;
        if (mobile.getDirection().equals(direction)) {
            to = new PointInTheWorld(mobile.getX() + direction.getDx(), mobile.getY() + direction.getDy(), mobile.getZ());
        } else {
            to = mobile;
        }
        return MovementResult.success(mobile, direction, to, running);
    }

    public MovementResult tryMove(UOMobile mobile, Location location) {
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

    public void applyMove(UOMobile mobile, MovementResult result) {
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
