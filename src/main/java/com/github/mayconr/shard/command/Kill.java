package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.TargetType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.WorldActions;
import com.github.mayconr.juoserver.game.world.WorldView;

public class Kill extends AbstractCommand {

    private final WorldActions worldActions;
    private final WorldView worldView;

    public Kill(WorldActions worldActions, WorldView worldView) {
        super("kill");
        this.worldActions = worldActions;
        this.worldView = worldView;
    }

    @Override
    public void handle(Prompt event) {
        worldActions.sendTarget(event.player(), CursorType.NEUTRAL, result->{
            if (TargetType.OBJECT.equals(result.type()) && UOPlayer.isMobile(result.serialId())) {
                final var mobile = worldView.getMobileBySerialId(result.serialId())
                        .orElseThrow(IllegalArgumentException::new);
                worldActions.deleteMobile(mobile);
                worldActions.sendMessage(event.player(), new PlainTextMessageContent(String.format("%s has been deleted", mobile.getDisplayName())));
            }
        });
    }
}
