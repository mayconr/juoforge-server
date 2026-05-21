package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.MobileTargetResult;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class SystemCmd extends AbstractCommand {

    private final World world;

    public SystemCmd(ServerRuntime runtime) {
        super("system");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        final var player = event.player();
        var arg = event.arguments()[0];

        switch (arg) {
            case "move" ->{
                world.sendTarget(player, CursorType.NEUTRAL, res->{
                    if (res instanceof MobileTargetResult mobileResult) {
                        world.move(mobileResult.mobile(), Direction.NORTH);
                    }

                });
            }
            case "detach" ->{
                world.sendTarget(player, CursorType.NEUTRAL, res->{
                    if (res instanceof MobileTargetResult mobileResult) {
                        world.detachAI((UONpc) mobileResult.mobile());
                    }

                });
            }
        }
    }
}
