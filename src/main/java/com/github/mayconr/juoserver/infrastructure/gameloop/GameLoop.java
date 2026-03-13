package com.github.mayconr.juoserver.infrastructure.gameloop;

public interface GameLoop {

    void addTask(GameTask task);

    void addTasks(GameTask... tasks);
}
