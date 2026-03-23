package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
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
            final var mobile = world.getMobileBySerialId(targetResult.serialId())
                    .orElseThrow(() -> new IllegalStateException("No mobile found"));

            world.applyDamage(new DamageRequest(event.player(), mobile, DamageSourceKind.COMMAND, List.of(new DamageComponent(DamageType.PHYSICAL, 50))));
        });

    }
}
