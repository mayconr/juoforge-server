package com.github.mayconr.juoserver.game.session.npc;

import com.github.mayconr.juoserver.game.gameloop.IntervalGameTask;

public class NpcVitalsTask extends IntervalGameTask {

    private final NpcSession session;

    public NpcVitalsTask(NpcSession session) {
        super(20);
        this.session = session;
    }

    @Override
    public void execute(double delta) {}

    @Override
    public boolean isDone() {
        // TODO return done when npc is dead
        return super.isDone();
    }
}
