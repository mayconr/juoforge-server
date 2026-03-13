package com.github.mayconr.juoserver.infrastructure.gameloop;

import com.github.mayconr.juoserver.JuoforgeConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class DefaultGameLoop implements GameLoop {

    private final List<GameTask> gameTasks = new ArrayList<>();
    private volatile boolean running = true;

    private final JuoforgeConfiguration configuration;

    @Override
    public void addTask(GameTask task) {
        synchronized (gameTasks) {
            this.gameTasks.add(task);
            log.debug("Game task [{}] added!", task);
        }
    }

    @Override
    public void addTasks(GameTask... tasks) {
        synchronized (gameTasks) {
            for (GameTask task : tasks) {
                this.gameTasks.add(task);
                log.debug("Game task [{}] added!", task);
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
                    double currentDelta = (now - lastTime) / 1_000_000_000.0;
                    lastTime = now;

                    currentDelta = Math.min(currentDelta, 0.5);

                    synchronized (gameTasks) {
                        final var iterator = gameTasks.iterator();
                        while (iterator.hasNext()) {
                            final var task = iterator.next();
                            try {
                                task.execute(currentTick, currentDelta);
                            } catch (Exception e) {
                                log.error("Task [{}] failed and it will be removed", task, e);
                                iterator.remove();
                            }

                            if (task.isDone()) {
                                iterator.remove();
                                task.onDone(currentTick, currentDelta);
                                log.debug("Game task [{}] removed!", task);
                            }
                        }
                    }

                    Thread.sleep(1000 / configuration.settings().gameLoop().tps());
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
