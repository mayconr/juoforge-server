package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.SpellbookType;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class DevTest extends AbstractCommand {
    private final ServerRuntime runtime;

    public DevTest(ServerRuntime runtime) {
        super("devtest");
        this.runtime = runtime;
    }

    @Override
    public void handle(Prompt event) {
        var region = runtime.world().region().getRegion(event.player());

        var ai = runtime.regionSystem().findAiActivationRegion(region.get()).orElseThrow();

        System.out.println(ai.getName());
    }
}
