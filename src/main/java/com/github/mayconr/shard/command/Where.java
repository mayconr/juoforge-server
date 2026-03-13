package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Where extends AbstractCommand{

    private final World world;

    public Where(World world) {
        super("where");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        var message = "You are here "+event.player().getX()+" - "+ event.player().getY()+" - "+event.player().getZ();
        world.sendMessage(event.player(), message, MessageOptions.standard());
        log.debug(message);
    }
}
