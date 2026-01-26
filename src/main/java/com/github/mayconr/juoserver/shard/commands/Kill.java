package com.github.mayconr.juoserver.shard.commands;

import com.github.mayconr.juoserver.common.event.HandlerResult;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.session.player.target.TargetType;
import com.github.mayconr.juoserver.game.session.world.WorldSession;

public class Kill extends AbstractCommand {

    private final WorldSession worldSession;

    public Kill(WorldSession worldSession) {
        super("kill");
        this.worldSession = worldSession;
    }

    @Override
    public HandlerResult handle(Prompt event) {
        final var playerSession = worldSession.getPlayerSession(event.mobile());

        playerSession.sendTarget(CursorType.NEUTRAL, target->{
            if (TargetType.OBJECT.equals(target.type()) && worldSession.isMobile(target.serialId())) {
                worldSession.deleteMobile(target.serialId());
            }
        });
        return HandlerResult.CONTINUE;
    }
}
