package com.github.mayconr.juoserver.game.gameloop;

public interface GameLoop {

    void addTask(GameTask task);

    void addTasks(GameTask... tasks);
}
