package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.MobileTargetResult;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.World;

public class NpcAi extends AbstractCommand {
    private final World world;

    public NpcAi(ServerRuntime runtime) {
        super("npcai");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        world.message().send(event.player(), "Select a mobile to inspect its AI.");
        world.interaction().sendTarget(event.player(), CursorType.NEUTRAL, result -> {
            if (result instanceof MobileTargetResult target) {
                var mobile = target.mobile();
                String message = "No AI installed";
                if (mobile instanceof UONpc npc) {
                    var status = world.ai().getAiStatus(npc);
                    message = "AI: " + (status.installed() ? status.type() : "none")
                            + " | Installed: " + status.installed() + " | Active: " + status.active();
                }
                world.message().printTextAbove(mobile, new PlainTextMessageContent(message), event.player());
            }
        });
    }
}
