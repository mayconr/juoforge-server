package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.EquipItemTarget;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class CreateEquippedItem extends AbstractCommand {

    private final World world;

    public CreateEquippedItem(World world) {
        super("createequippeditem");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.createItem(ItemRequest.byName(event.arguments()[0]), EquipItemTarget.of(event.player()));
    }
}
