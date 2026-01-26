package com.github.mayconr.juoserver.shard.commands;

import com.github.mayconr.juoserver.common.event.HandlerResult;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateNpc extends AbstractCommand {

    private final WorldSession worldSession;

    public CreateNpc(WorldSession worldSession) {
        super("createnpc");
        this.worldSession = worldSession;
    }

    @Override
    public HandlerResult handle(Prompt event) {
        worldSession.createNpcAtLocation(event.arguments()[0], event.mobile())
                .whenComplete(((uoNpc, throwable) -> {
                    if (throwable != null) {
                        log.error("error ",throwable);
                    }
                }));
        return HandlerResult.CONTINUE;
    }
}
