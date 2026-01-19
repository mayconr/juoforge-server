package com.github.mayconr.juoserver.game.gameloop;

public abstract class IntervalGameTask implements GameTask {
    private final int intervalTicks;
    private int counter;

    public IntervalGameTask(int intervalTicks) {
        if (intervalTicks <= 0) {
            throw new IllegalArgumentException("Interval must be > 0");
        }
        this.intervalTicks = intervalTicks;
        this.counter = intervalTicks; // inicia com intervalo cheio
    }

    @Override
    public void execute(long currentTick) {
        counter--;
        if (counter <= 0) {
            execute();
            counter = intervalTicks; // reinicia o contador
        }
    }

    public abstract void execute();

    @Override
    public boolean isDone() {
        return false;
    }
}
