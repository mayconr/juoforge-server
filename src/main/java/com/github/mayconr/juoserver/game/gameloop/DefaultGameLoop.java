package com.github.mayconr.juoserver.game.gameloop;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultGameLoop implements GameLoop {

    private static final int TPS = 20;
    private final List<GameTask> gameTasks = new ArrayList<>();
    private volatile boolean running = true;

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
        new Thread(() -> {

            long currentTick = 0;
            long lastTime = System.nanoTime();

            log.info("Gameloop started");

            while (running) {
                try {
                    long now = System.nanoTime();
                    double deltaSeconds = (now - lastTime) / 1_000_000_000.0;
                    lastTime = now;

                    deltaSeconds = Math.min(deltaSeconds, 0.5);

                    synchronized (gameTasks) {
                        final var iterator = gameTasks.iterator();
                        while (iterator.hasNext()) {
                            final var task = iterator.next();
                            task.execute(currentTick, deltaSeconds);
                            if (task.isDone()) {
                                iterator.remove();
                                log.info("Game task [{}] removed!", task);
                            }
                        }
                    }

                    Thread.sleep(1000 / TPS);
                    currentTick++;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            log.info("Gameloop stopped");

        }, "gameloop").start();
        return this;
    }

    public void stop() {
        running = false;
    }
}
