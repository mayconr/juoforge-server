package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class Destroy extends AbstractCommand {

    private final World world;

    public Destroy(World world) {
        super("destroy");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.sendTarget(event.player(), CursorType.NEUTRAL, target->{
            if (UOItem.isItem(target.serialId())) {
                world.deleteItem(target.serialId());
            }
        });
    }
}
