package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.TileTargetResult;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.world.World;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateNpc extends AbstractCommand {
    private final World world;

    public CreateNpc(World world) {
        super("createnpc");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        final var player = event.player();
        final var npcName = event.arguments()[0];

        world.sendMessage(player, MessageContent.plain("Select a location to create the NPC"));
        world.sendTarget(event.player(), CursorType.NEUTRAL, result->{
            if (result instanceof TileTargetResult staticResult) {
                System.out.println(staticResult.staticsTile());
            }
            world.createNpc(npcName, result.location());
        });

    }
}
