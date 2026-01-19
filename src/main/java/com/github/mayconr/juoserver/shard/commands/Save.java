package com.github.mayconr.juoserver.shard.commands;

import com.github.mayconr.juoserver.common.event.HandlerResult;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.world.WorldService;

public class Save extends AbstractCommand {
    private final WorldService worldService;

    public Save(WorldService worldService) {
        super("save");
        this.worldService = worldService;
    }

    @Override
    public HandlerResult handle(Prompt event) {
        worldService.saveMobileRuntime();
        worldService.saveMobileAttributes();
        worldService.saveMobileVitals();
        worldService.saveMobiles();
        return HandlerResult.CONTINUE;
    }
}
