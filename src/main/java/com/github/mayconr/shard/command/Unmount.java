package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.WorldActions;

public class Unmount extends AbstractCommand{

    private final WorldActions world;

    public Unmount(WorldActions world) {
        super("unmount");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.unmount(event.player());
    }
}
