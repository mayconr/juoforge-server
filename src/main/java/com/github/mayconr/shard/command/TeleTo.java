package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.world.MobileActions;
import com.github.mayconr.juoserver.game.session.world.PlayerActions;

public class TeleTo extends AbstractCommand {

    private final MobileActions mobileActions;
    private final PlayerActions playerActions;

    public TeleTo(MobileActions mobileActions, PlayerActions playerActions) {
        super("teleto");
        this.mobileActions = mobileActions;
        this.playerActions = playerActions;
    }

    @Override
    public void handle(Prompt event) {
        if (event.mobile() instanceof  UOPlayer player) {
            playerActions.sendTarget(player, CursorType.HARMFUL, res->{
                if (res.isStatics()) {
                    mobileActions.teleport(event.mobile(), res.location());
                }
            });
        }
    }
}
