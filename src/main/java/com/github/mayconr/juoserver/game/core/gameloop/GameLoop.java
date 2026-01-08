package com.github.mayconr.juoserver.game.core.gameloop;

public interface GameLoop {

    void addTask(GameTask task);

    void addTasks(GameTask... tasks);
}
