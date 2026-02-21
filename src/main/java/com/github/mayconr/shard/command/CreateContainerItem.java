package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.ItemOptions;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class CreateContainerItem extends AbstractCommand{

    private final World world;
    
    public CreateContainerItem(World world) {
        super("createContainerItem");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.createItem(event.arguments()[0], ItemOptions.builder()
                .target(new ItemOptions.ContainerTarget(event.player()))
                .build());
    }
}
