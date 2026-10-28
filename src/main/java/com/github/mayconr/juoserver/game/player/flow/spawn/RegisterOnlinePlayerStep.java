package com.github.mayconr.juoserver.game.player.flow.spawn;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class RegisterOnlinePlayerStep extends AbstractFlowStep<PlayerSpawnContext> {
    public RegisterOnlinePlayerStep() {
        super("RegisterOnlinePlayer");
    }

    @Override
    public StepResult execute(PlayerSpawnContext context) {
        var player = context.getPlayer();
        context.getOnlinePlayers().put(player.getSerialId(), player);
        return StepResult.success();
    }
}
