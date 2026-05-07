package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class Info extends AbstractCommand{

    private final World world;

    public Info(World world) {
        super("info");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.sendTarget(event.player(), CursorType.NEUTRAL, res->{

        });
    }
}
