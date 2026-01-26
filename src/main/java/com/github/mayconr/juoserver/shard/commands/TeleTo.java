package com.github.mayconr.juoserver.shard.commands;

import com.github.mayconr.juoserver.common.event.HandlerResult;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.session.world.WorldSession;

public class TeleTo extends AbstractCommand {

    private final WorldSession worldSession;

    public TeleTo(WorldSession worldSession) {
        super("teleto");
        this.worldSession = worldSession;
    }

    @Override
    public HandlerResult handle(Prompt event) {
        final var playerSession = worldSession.getPlayerSession(event.mobile());
        playerSession.sendTarget(CursorType.HARMFUL, res->{
            playerSession.move(res.location());
        });
        //playerSession.move();

        return HandlerResult.CONTINUE;
    }
}
