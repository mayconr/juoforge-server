package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.WorldActions;
import com.github.mayconr.juoserver.game.world.WorldView;

public class Mount extends AbstractCommand{

    private final WorldActions worldActions;
    private final WorldView worldView;

    public Mount(WorldActions worldActions, WorldView worldView) {
        super("mount");
        this.worldActions = worldActions;
        this.worldView = worldView;
    }

    @Override
    public void handle(Prompt event) {
        worldActions.sendTarget((UOPlayer) event.player(), CursorType.NEUTRAL, result->{
            worldView.getMobileBySerialId(result.serialId())
                .ifPresent(mobile->{
                    if (mobile instanceof UONpc npc) {
                        worldActions.mount((UOPlayer) event.player(), npc);
                    }
                });
        });
    }
}
