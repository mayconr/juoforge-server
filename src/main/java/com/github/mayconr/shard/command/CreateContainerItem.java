package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.WorldInternal;

public class CreateContainerItem extends AbstractCommand{

    private final WorldInternal world;
    public CreateContainerItem(WorldInternal world) {
        super("createContainerItem");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.createContainerItem(event.arguments()[0], event.player());
    }
}
