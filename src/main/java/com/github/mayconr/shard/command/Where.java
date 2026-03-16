package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.World;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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

        //world.sendMessage(event.player(), new PlainTextMessageContent(message), MessageOptions.standard());

        world.sendTarget(event.player(), CursorType.NEUTRAL, result->{
            world.getMobileBySerialId(result.serialId()).ifPresent(mobile -> {
                world.printTextAbove(mobile, new PlainTextMessageContent(message), event.player());
            });
            world.getItemBySerialId(result.serialId()).ifPresent(item->{
                world.printTextAbove(item, new PlainTextMessageContent(message));
            });
        });
        world.sendMessage(event.player(), MessageContent.localized(message, Map.of()));

        log.debug(message);
    }
}
