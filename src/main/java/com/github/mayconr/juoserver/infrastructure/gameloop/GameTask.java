package com.github.mayconr.juoserver.infrastructure.gameloop;

public interface GameTask {

    void execute(long currentTick, double delta);

    boolean isDone();

    default void onDone(long currentTick, double delta) {

    }
}
