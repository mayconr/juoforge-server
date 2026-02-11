package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldActions;

public class TeleTo extends AbstractCommand {

    private final WorldActions worldActions;

    public TeleTo(WorldActions worldActions) {
        super("teleto");
        this.worldActions = worldActions;
    }

    @Override
    public void handle(Prompt event) {
        if (event.player() instanceof  UOPlayer player) {
            worldActions.sendTarget(player, CursorType.HARMFUL, res->{
                if (res.isStatics()) {
                    worldActions.teleport(event.player(), res.location());
                }
            });
        }
    }
}
