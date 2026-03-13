package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class Region extends AbstractCommand{

    private final World world;

    public Region(World world) {
        super("region");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        var region = world.getRegion(event.player())
                .orElseThrow(()->new IllegalStateException("Region has no region"));
        world.sendMessage(event.player(), "Voce esta em "+region.getDisplayName(), MessageOptions.standard());
    }
}
