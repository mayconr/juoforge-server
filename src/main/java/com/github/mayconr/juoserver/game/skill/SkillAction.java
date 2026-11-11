package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.Location;

/**
 * A validated action with a captured swing count and simulation-time delay.
 * The actor's position is compared to the origin captured when the action was requested.
 */
public final class SkillAction {
    private final Location actor;
    private final Location origin;
    private int swingsLeft;
    private int swingsExecuted;
    private double remainingSeconds;
    private final double delaySeconds;
    private final SkillCallback onSwing;
    private final SkillCallback onComplete;
    private volatile boolean stopRequested;

    public SkillAction(Location actor, Location origin, int swings, double delaySeconds,
                       SkillCallback onSwing, SkillCallback onComplete) {
        this.actor = actor;
        this.origin = origin;
        this.swingsLeft = swings;
        this.delaySeconds = delaySeconds;
        this.onSwing = onSwing;
        this.onComplete = onComplete;
    }

    public boolean advance(double delta) {
        remainingSeconds -= delta;
        return remainingSeconds <= 1e-9;
    }

    /** Whether the actor left the tile where the action was requested. */
    public boolean hasMoved() {
        return actor.getX() != origin.getX() || actor.getY() != origin.getY();
    }

    /** Marks the action to end on the next update, without interrupting a swing in progress. */
    public void requestStop() {
        stopRequested = true;
    }

    /** Whether the remaining swings must be skipped, either by request or because the actor moved. */
    public boolean shouldCancel() {
        return stopRequested || hasMoved();
    }

    public void executeSwing() {
        onSwing.execute(++swingsExecuted);
        swingsLeft--;
        remainingSeconds = delaySeconds;
    }

    public boolean isComplete() {
        return swingsLeft == 0;
    }

    public void complete() {
        onComplete.execute(swingsExecuted);
    }
}
