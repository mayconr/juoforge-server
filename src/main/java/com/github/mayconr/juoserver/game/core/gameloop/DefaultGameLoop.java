package com.github.mayconr.juoserver.game.core.gameloop;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultGameLoop implements GameLoop {

    private static final int TPS = 20;
    private final List<GameTask> gameTasks = new ArrayList<>();
    private boolean running = true;

    @Override
    public void addTask(GameTask task) {
        synchronized (gameTasks) {
            this.gameTasks.add(task);
            log.info("Game task [{}] added!", task);
        }
    }

    @Override
    public void addTasks(GameTask... tasks) {
        synchronized (gameTasks) {
            for (GameTask task : tasks) {
                this.gameTasks.add(task);
                log.info("Game task [{}] added!", task);
            }
        }
    }

    public DefaultGameLoop start() {
        new Thread(
                        () -> {
                            long currentTick = 0;
                            log.info("Gameloop started");
                            while (running) {
                                try {
                                    synchronized (gameTasks) {
                                        final var iterator = gameTasks.iterator();
                                        while (iterator.hasNext()) {
                                            final var task = iterator.next();
                                            task.execute(currentTick);
                                            if (task.isDone()) {
                                                iterator.remove();
                                                log.info("Game task [{}] removed!", task);
                                            }
                                        }
                                    }
                                    Thread.sleep(1000 / TPS);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                currentTick++;
                            }
                            log.info("Gameloop stopped");
                        },
                        "gameloop")
                .start();
        return this;
    }

    public void stop() {
        running = false;
    }
}
