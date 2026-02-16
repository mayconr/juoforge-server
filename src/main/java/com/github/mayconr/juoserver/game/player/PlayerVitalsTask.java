package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.infrastructure.gameloop.IntervalGameTask;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldInternal;

public class PlayerVitalsTask extends IntervalGameTask {

    private final UOPlayer player;
    private final WorldInternal world;

    public PlayerVitalsTask(UOPlayer player, WorldInternal world) {
        super(10);
        this.player = player;
        this.world = world;
    }

    @Override
    public boolean isDone() {
        return !player.isConnected();
    }

    @Override
    public void execute(double delta) {
        world.regen(player, delta);
    }

    @Override
    public String toString() {
        return "PlayerVitals " + player.getName();
    }
}
