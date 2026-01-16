package com.github.mayconr.juoserver.game.server;

import io.netty.util.concurrent.EventExecutor;

public class SessionEventLoop {

    private final EventExecutor executor;

    public SessionEventLoop(EventExecutor executor) {
        this.executor = executor;
    }

    public void run(Runnable task) {
        if (executor.inEventLoop()) {
            task.run();
        } else {
            executor.execute(task);
        }
    }
}
