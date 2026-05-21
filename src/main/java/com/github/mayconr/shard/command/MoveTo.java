package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.MobileTargetResult;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class MoveTo extends AbstractCommand{

    private final World world;

    public MoveTo(World world) {
        super("moveto");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.sendTarget(event.player(), CursorType.NEUTRAL, t1->{
            if (t1 instanceof MobileTargetResult result) {
                world.sendTarget(event.player(), CursorType.NEUTRAL, t2->{
                   world.teleport(result.mobile(), t2.location());
                });
            }
        });
    }
}
