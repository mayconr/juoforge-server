package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateItem extends AbstractCommand {

    private final World world;

    public CreateItem(World world) {
        super("createitem");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.createItem(ItemRequest.byName(event.arguments()[0]), ContainerItemTarget.of(event.player(), cfg->{
            cfg.tryStack(true);
        }));
    }
}
