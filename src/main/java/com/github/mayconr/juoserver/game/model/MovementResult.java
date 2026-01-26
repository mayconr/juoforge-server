package com.github.mayconr.juoserver.game.model;

public record MovementResult(
        boolean success,
        Direction direction,
        Location from,
        Location to,
        boolean running,
        MovementFailureReason failureReason
) {

    public static MovementResult denied(
            UOMobile mobile,
            MovementFailureReason reason
    ) {
        return new MovementResult(
                false,
                mobile.getDirection(),
                new PointInTheWorld(mobile),
                new PointInTheWorld(mobile),
                mobile.isRunning(),
                reason
        );
    }

    public static MovementResult success(
            UOMobile mobile,
            Direction direction,
            Location to,
            boolean running
    ) {
        return new MovementResult(
                true,
                direction,
                new PointInTheWorld(mobile),
                to,
                running,
                null
        );
    }
}
