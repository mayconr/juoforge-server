package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public final class OpenPaperdoll extends AbstractCommand {
    private final World world;

    public OpenPaperdoll(ServerRuntime runtime) {
        super("openpaperdoll");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        world.ui().openPaperdoll(event.player(), event.player().getSerialId());
    }
}
