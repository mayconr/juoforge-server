package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

import java.util.Comparator;

public class ActiveAi extends AbstractCommand {
    private final World world;

    public ActiveAi(ServerRuntime runtime) {
        super("activeai");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        var npcs = world.ai().getActiveNpcs().stream()
                .sorted(Comparator.comparingInt(UONpc::getSerialId))
                .toList();
        world.message().send(event.player(), "NPCs with active AI: " + npcs.size());
        for (var npc : npcs) {
            world.message().send(event.player(), "ID: " + npc.getSerialId() + " | Name: " + npc.getName());
        }
    }
}
