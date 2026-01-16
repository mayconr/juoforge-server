package com.github.mayconr.juoserver.game.shard.commands;

import com.github.mayconr.juoserver.game.core.event.HandlerResult;
import com.github.mayconr.juoserver.game.core.event.Prompt;
import com.github.mayconr.juoserver.game.storage.WorldService;

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
