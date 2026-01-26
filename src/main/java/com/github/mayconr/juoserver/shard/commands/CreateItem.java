package com.github.mayconr.juoserver.shard.commands;

import com.github.mayconr.juoserver.common.event.HandlerResult;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateItem extends AbstractCommand {
    private final WorldSession worldSession;

    public CreateItem(WorldSession worldSession) {
        super("createitem");
        this.worldSession = worldSession;
    }

    @Override
    public HandlerResult handle(Prompt event) {
        worldSession.createItemAtLocation(event.arguments()[0], event.mobile())
                .whenComplete(((item, throwable) -> {
                    if (throwable != null) {
                        log.error("erro ", throwable);
                    }
                }));
        return HandlerResult.CONTINUE;
    }
}
