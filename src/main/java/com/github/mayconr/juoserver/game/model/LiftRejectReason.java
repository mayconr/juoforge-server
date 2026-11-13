package com.github.mayconr.juoserver.game.model;

/**
 * Reason sent to the client when a lift request is refused. The client puts the item back where it was and,
 * for every reason except {@link #INSPECIFIC}, shows the matching system message.
 */
public enum LiftRejectReason {
    CANNOT_LIFT(0),
    OUT_OF_RANGE(1),
    OUT_OF_SIGHT(2),
    TRY_TO_STEAL(3),
    ARE_HOLDING(4),
    INSPECIFIC(5);

    private final int code;

    LiftRejectReason(int code) {
        this.code = code;
    }

    /** Protocol value of the reason. */
    public int code() {
        return code;
    }
}
