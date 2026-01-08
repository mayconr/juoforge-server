package com.github.mayconr.juoserver.game.core.gameloop;

public interface GameTask {

    void execute(long currentTick);

    boolean isDone();
}
