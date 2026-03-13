package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.item.ItemCreationRequest;
import com.github.mayconr.juoserver.game.model.ItemOptions;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class CreateStack extends AbstractCommand{

    private final World world;

    public CreateStack(World world) {
        super("createstack");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.createItem(ItemCreationRequest.byName(event.arguments()[0]).amount(Integer.parseInt(event.arguments()[1])).build(), ItemOptions.builder()
                .target(new ItemOptions.ContainerTarget(event.player().getBackpack()))
                .build());
    }
}
