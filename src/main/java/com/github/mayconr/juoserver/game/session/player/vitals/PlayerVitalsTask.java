package com.github.mayconr.juoserver.game.session.player.vitals;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.game.gameloop.IntervalGameTask;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;

public class PlayerVitalsTask extends IntervalGameTask {

    private final PlayerSession session;
    private final VitalsService vitalsService;

    public PlayerVitalsTask(PlayerSession session, VitalsService vitalsService, ServerProperties properties) {
        super(10);
        this.session = session;
        this.vitalsService = vitalsService;
    }

    @Override
    public boolean isDone() {
        return !session.isActive();
    }

    @Override
    public void execute(double delta) {
        vitalsService.regen(delta);
    }

    @Override
    public String toString() {
        return "PlayerVitals " + session.getPlayer().getName();
    }
}
