package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.World;

public class Region extends AbstractCommand{

    private final World world;

    public Region(ServerRuntime runtime) {
        super("region");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        var region = world.getRegion(event.player())
                .orElseThrow(()->new IllegalStateException("Region has no region"));
        world.sendMessage(event.player(), new PlainTextMessageContent("Voce esta em "+region.getDisplayName()));
    }
}
