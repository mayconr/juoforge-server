package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class TeleTo extends AbstractCommand {
    private final World world;

    public TeleTo(World world) {
        super("teleto");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        if (event.player() instanceof  UOPlayer player) {
            world.sendTarget(player, CursorType.HARMFUL, res->{
                if (res.isStatics()) {
                    world.move(event.player(), res.location());
                }
            });
        }
    }
}
