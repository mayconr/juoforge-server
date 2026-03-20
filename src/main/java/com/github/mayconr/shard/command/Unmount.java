package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class Unmount extends AbstractCommand{

    private final World world;

    public Unmount(ServerRuntime runtime) {
        super("unmount");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        world.unmount(event.player());
    }
}
