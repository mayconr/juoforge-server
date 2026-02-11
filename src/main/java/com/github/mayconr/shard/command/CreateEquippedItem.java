package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.WorldActions;

public class CreateEquippedItem extends AbstractCommand {

    private final WorldActions worldActions;

    public CreateEquippedItem(WorldActions world) {
        super("createequippeditem");
        this.worldActions = world;
    }

    @Override
    public void handle(Prompt event) {
        worldActions.createEquippedItem(event.player(), event.arguments()[0]);
    }
}
