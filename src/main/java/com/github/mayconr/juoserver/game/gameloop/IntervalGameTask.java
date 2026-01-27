package com.github.mayconr.juoserver.game.gameloop;

public abstract class IntervalGameTask implements GameTask {
    private final int intervalTicks;
    private int counter;

    public IntervalGameTask(int intervalTicks) {
        if (intervalTicks <= 0) {
            throw new IllegalArgumentException("Interval must be > 0");
        }
        this.intervalTicks = intervalTicks;
        this.counter = intervalTicks;
    }

    @Override
    public void execute(long currentTick, double delta) {
        counter--;
        if (counter <= 0) {
            execute(delta);
            counter = intervalTicks;
        }
    }

    public abstract void execute(double delta);

    @Override
    public boolean isDone() {
        return false;
    }
}
