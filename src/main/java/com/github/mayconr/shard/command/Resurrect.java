package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.MobileTargetResult;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class Resurrect extends AbstractCommand{

    private final World world;

    public Resurrect(ServerRuntime runtime) {
        super("res");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        world.sendTarget(event.player(), CursorType.NEUTRAL, targetResult -> {
            if (targetResult instanceof MobileTargetResult result) {
                world.resurrect(result.mobile());
            }
        });
    }
}
