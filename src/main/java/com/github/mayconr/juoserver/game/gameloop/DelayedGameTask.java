package com.github.mayconr.juoserver.game.gameloop;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DelayedGameTask implements GameTask {

    private int remainingTicks;
    private final Runnable action;

    @Override
    public void execute(long currentTick) {
        if (remainingTicks > 0) {
            remainingTicks--;
        }

        if (remainingTicks == 0) {
            action.run();
        }
    }

    @Override
    public boolean isDone() {
        return remainingTicks <= 0;
    }
}
