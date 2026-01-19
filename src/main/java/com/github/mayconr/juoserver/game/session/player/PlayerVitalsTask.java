package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.gameloop.IntervalGameTask;

public class PlayerVitalsTask extends IntervalGameTask {
    private final PlayerSession session;

    public PlayerVitalsTask(PlayerSession session) {
        super(20);
        this.session = session;
    }

    @Override
    public boolean isDone() {
        return !session.isActive();
    }

    @Override
    public void execute() {
        // TODO recalculate vitals
    }

    @Override
    public String toString() {
        return "PlayerVitals " + session.getPlayer().getName();
    }
}
