package com.github.mayconr.juoserver.game.world.module.ai.session;

import com.github.mayconr.juoserver.game.world.World;

public interface AISession {

    void wakeup(World world);

    void kill();

    void think(double delta);
}
