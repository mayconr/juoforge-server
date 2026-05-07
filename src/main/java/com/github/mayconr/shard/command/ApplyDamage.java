package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.MobileTargetResult;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.DamageRequest;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.DamageType;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

import java.util.List;

public class ApplyDamage extends AbstractCommand {

    private final World world;

    public ApplyDamage(ServerRuntime runtime) {
        super("applyDamage");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        world.sendTarget(event.player(), CursorType.HARMFUL, targetResult -> {
            if (targetResult instanceof MobileTargetResult rs) {
                world.applyDamage(new DamageRequest(event.player(), rs.mobile(), DamageSourceKind.COMMAND, List.of(new DamageComponent(DamageType.PHYSICAL, Integer.parseInt(event.arguments()[0])))));
            }
        });

    }
}
