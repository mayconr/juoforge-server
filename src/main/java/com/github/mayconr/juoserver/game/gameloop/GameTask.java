package com.github.mayconr.juoserver.game.gameloop;

public interface GameTask {

    void execute(long currentTick);

    boolean isDone();
}
