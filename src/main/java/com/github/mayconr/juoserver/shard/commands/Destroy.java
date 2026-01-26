package com.github.mayconr.juoserver.shard.commands;

import com.github.mayconr.juoserver.common.event.HandlerResult;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.session.world.WorldSession;

public class Destroy extends AbstractCommand {

    private final WorldSession worldSession;

    public Destroy(WorldSession worldSession) {
        super("destroy");
        this.worldSession = worldSession;
    }

    @Override
    public HandlerResult handle(Prompt event) {
        final var playerSession = worldSession.getPlayerSession(event.mobile());

        playerSession.sendTarget(CursorType.NEUTRAL, target->{
            if (worldSession.isItem(target.serialId())) {
                worldSession.deleteItem(target.serialId());
            }
        });
        return HandlerResult.CONTINUE;
    }
}
