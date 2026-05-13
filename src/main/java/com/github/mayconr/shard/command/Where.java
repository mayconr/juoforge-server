package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.ItemTargetResult;
import com.github.mayconr.juoserver.game.model.MobileTargetResult;
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

    public Where(ServerRuntime runtime) {
        super("where");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        var message = "You are here "+event.player().getX()+" - "+ event.player().getY()+" - "+event.player().getZ();

        world.sendTarget(event.player(), CursorType.NEUTRAL, result->{
            if (result instanceof MobileTargetResult rs) {
                world.printTextAbove(rs.mobile(), new PlainTextMessageContent(message), event.player());
            }
            if (result instanceof ItemTargetResult rs) {
                world.printTextAbove(rs.item(), new PlainTextMessageContent(message));
            }
        });
        world.sendMessage(event.player(), MessageContent.localized(message, Map.of()));

        log.debug(message);
    }
}
