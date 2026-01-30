package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.session.world.WorldActions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateItem extends AbstractCommand {
    private final WorldActions worldActions;

    public CreateItem(WorldActions worldActions) {
        super("createitem");
        this.worldActions = worldActions;
    }

    @Override
    public void handle(Prompt event) {
        worldActions.createItem(event.arguments()[0], event.mobile());
    }
}
