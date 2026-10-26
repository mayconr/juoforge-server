package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.world.World;

/** Sends the command arguments as plain text to every online player. */
public class Broadcast extends AbstractCommand {
    private final World world;

    public Broadcast(ServerRuntime runtime) {
        super("broadcast");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        var text = String.join(" ", event.arguments()).trim();
        if (text.isEmpty()) {
            world.message().send(event.player(), "Usage: .broadcast <message>");
            return;
        }

        var message = MessageContent.plain(text);
        for (var player : world.player().getOnlinePlayers()) {
            world.message().send(player, message);
        }
    }
}
