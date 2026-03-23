package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.TargetType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
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
            if (TargetType.OBJECT.equals(result.type()) && UOPlayer.isMobile(result.serialId())) {
                final var mobile = world.getMobileBySerialId(result.serialId())
                        .orElseThrow(IllegalArgumentException::new);
                world.kill(mobile, event.player(), DamageSourceKind.COMMAND);
                world.sendMessage(event.player(), new PlainTextMessageContent(String.format("%s has been killed", mobile.getDisplayName())));
            }
        });
    }
}
