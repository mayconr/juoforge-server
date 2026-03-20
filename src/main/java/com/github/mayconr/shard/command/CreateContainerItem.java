package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class CreateContainerItem extends AbstractCommand{

    private final World world;

    public CreateContainerItem(ServerRuntime  serverRuntime) {
        super("createContainerItem");
        this.world = serverRuntime.world();
    }

    @Override
    public void handle(Prompt event) {
        world.createItem(ItemRequest.byName(event.arguments()[0]).build(), ContainerItemTarget.of(event.player()));
    }
}
