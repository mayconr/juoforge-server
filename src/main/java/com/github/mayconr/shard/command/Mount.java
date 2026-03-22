package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class Mount extends AbstractCommand{

    private final World world;

    public Mount(ServerRuntime runtime) {
        super("mount");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        world.sendTarget(event.player(), CursorType.NEUTRAL, result->{
            world.getMobileBySerialId(result.serialId())
                .ifPresent(mobile->{
                    if (mobile instanceof UONpc npc) {
                        world.mount(event.player(), npc);
                    }
                });
        });
    }
}
