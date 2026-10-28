package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class AiTypes extends AbstractCommand {
    private final World world;

    public AiTypes(ServerRuntime runtime) {
        super("aitypes");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        var types = world.ai().getRegisteredTypes().stream().sorted().toList();
        world.message().send(event.player(), "Registered AI types: " + types.size());
        for (var type : types) {
            world.message().send(event.player(), type);
        }
    }
}
