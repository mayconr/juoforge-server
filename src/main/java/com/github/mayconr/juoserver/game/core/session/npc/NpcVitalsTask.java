package com.github.mayconr.juoserver.game.core.session.npc;

import com.github.mayconr.juoserver.game.core.gameloop.IntervalGameTask;

public class NpcVitalsTask extends IntervalGameTask {

    private final NpcSession session;

    public NpcVitalsTask(NpcSession session) {
        super(20);
        this.session = session;
    }

    @Override
    public void execute() {}

    @Override
    public boolean isDone() {
        // TODO return done when npc is dead
        return super.isDone();
    }
}
