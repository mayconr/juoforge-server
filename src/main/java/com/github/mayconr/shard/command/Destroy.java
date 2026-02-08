package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.world.WorldActions;

public class Destroy extends AbstractCommand {

    private final WorldActions worldActions;

    public Destroy(WorldActions worldActions) {
        super("destroy");
        this.worldActions = worldActions;
    }

    @Override
    public void handle(Prompt event) {
        worldActions.sendTarget((UOPlayer) event.mobile(), CursorType.NEUTRAL, target->{
            if (UOItem.isItem(target.serialId())) {
                worldActions.deleteItem(target.serialId());
            }
        });
    }
}
