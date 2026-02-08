package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.session.world.WorldActions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateNpc extends AbstractCommand {

    private final WorldActions actions;

    public CreateNpc(WorldActions actions) {
        super("createnpc");
        this.actions = actions;
    }

    @Override
    public void handle(Prompt event) {
        actions.createNpc(event.arguments()[0], event.mobile());
    }
}
