package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.ItemOptions;
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
        worldActions.createItem(event.arguments()[0], ItemOptions.builder()
                .target(new ItemOptions.EquipTarget(event.player()))
                .build());
        //worldActions.createEquippedItem(event.player(), event.arguments()[0]);
    }
}
