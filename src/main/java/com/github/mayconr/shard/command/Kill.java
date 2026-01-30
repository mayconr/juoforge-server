package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.player.target.TargetType;
import com.github.mayconr.juoserver.game.session.world.MobileActions;
import com.github.mayconr.juoserver.game.session.world.PlayerActions;
import com.github.mayconr.juoserver.game.session.world.WorldView;

public class Kill extends AbstractCommand {

    private final PlayerActions playerActions;
    private final MobileActions mobileActions;
    private final WorldView worldView;

    public Kill(PlayerActions playerActions, MobileActions mobileActions, WorldView worldView) {
        super("kill");
        this.playerActions = playerActions;
        this.mobileActions = mobileActions;
        this.worldView = worldView;
    }

    @Override
    public void handle(Prompt event) {
        playerActions.sendTarget((UOPlayer) event.mobile(), CursorType.NEUTRAL, result->{
            if (TargetType.OBJECT.equals(result.type()) && UOPlayer.isMobile(result.serialId())) {
                final var mobile = worldView.getMobileBySerialId(result.serialId())
                        .orElseThrow(IllegalArgumentException::new);
                mobileActions.deleteMobile(mobile);
                playerActions.sendMessage((UOPlayer) event.mobile(), String.format("%s has been deleted", mobile.getDisplayName()), MessageOptions.of(TextType.EMOTE,105, 0));
            }
        });
    }
}
