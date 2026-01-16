package com.github.mayconr.juoserver.game.core.model;

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
            Direction direction,
            MovementFailureReason reason
    ) {
        return new MovementResult(
                false,
                direction,
                mobile,
                mobile,
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
                mobile,
                to,
                running,
                null
        );
    }
}
