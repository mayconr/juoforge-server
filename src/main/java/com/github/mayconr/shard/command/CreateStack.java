package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget;
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
        world.createItem(ItemRequest.byName(event.arguments()[0]).withAmount(Integer.parseInt(event.arguments()[1])), ContainerItemTarget.of(event.player().getBackpack()));
    }
}
