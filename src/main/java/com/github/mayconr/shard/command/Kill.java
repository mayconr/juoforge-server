package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.player.target.TargetType;
import com.github.mayconr.juoserver.game.session.world.WorldActions;
import com.github.mayconr.juoserver.game.session.world.WorldView;

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
        worldActions.sendTarget((UOPlayer) event.mobile(), CursorType.NEUTRAL, result->{
            if (TargetType.OBJECT.equals(result.type()) && UOPlayer.isMobile(result.serialId())) {
                final var mobile = worldView.getMobileBySerialId(result.serialId())
                        .orElseThrow(IllegalArgumentException::new);
                worldActions.deleteMobile(mobile);
                worldActions.sendMessage((UOPlayer) event.mobile(), String.format("%s has been deleted", mobile.getDisplayName()), MessageOptions.of(TextType.EMOTE,105, 0));
            }
        });
    }
}
