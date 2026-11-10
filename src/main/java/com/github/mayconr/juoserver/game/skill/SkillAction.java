package com.github.mayconr.juoserver.game.skill;

/** A validated action with a captured swing count and simulation-time delay. */
public final class SkillAction {
    private int swingsLeft;
    private int swingsExecuted;
    private double remainingSeconds;
    private final double delaySeconds;
    private final SkillCallback swing;
    private final SkillCallback onComplete;

    public SkillAction(int swings, double delaySeconds, SkillCallback swing, SkillCallback onComplete) {
        this.swingsLeft = swings;
        this.delaySeconds = delaySeconds;
        this.swing = swing;
        this.onComplete = onComplete;
    }

    public boolean advance(double delta) {
        remainingSeconds -= delta;
        return remainingSeconds <= 1e-9;
    }

    public void executeSwing() {
        swing.execute(++swingsExecuted);
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
