package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.MobileTargetResult;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.World;

public class Kill extends AbstractCommand {

    private final World world;

    public Kill(World world) {
        super("kill");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        world.sendTarget(event.player(), CursorType.NEUTRAL, result->{
            if (result instanceof MobileTargetResult rs) {
                world.kill(rs.mobile(), event.player(), DamageSourceKind.COMMAND);
                world.sendMessage(event.player(), new PlainTextMessageContent(String.format("%s has been killed", rs.mobile().getDisplayName())));
            }
        });
    }
}
