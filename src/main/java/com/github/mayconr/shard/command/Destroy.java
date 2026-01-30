package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.world.ItemActions;
import com.github.mayconr.juoserver.game.session.world.PlayerActions;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;

public class Destroy extends AbstractCommand {

    private final PlayerActions playerActions;
    private final ItemActions itemActions;

    public Destroy(PlayerActions playerActions, ItemActions itemActions) {
        super("destroy");
        this.playerActions = playerActions;
        this.itemActions = itemActions;
    }

    @Override
    public void handle(Prompt event) {
        playerActions.sendTarget((UOPlayer) event.mobile(), CursorType.NEUTRAL, target->{
            if (UOItem.isItem(target.serialId())) {
                itemActions.deleteItem(target.serialId());
            }
        });
    }
}
