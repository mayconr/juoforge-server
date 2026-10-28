package com.github.mayconr.juoserver.game.player.flow.spawn;

import com.github.mayconr.juoserver.game.region.shared.UpdateMobileRegionStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class PlayerSpawnFlowDefinition {
    private PlayerSpawnFlowDefinition() {}

    public static Flow<PlayerSpawnContext> build(GameModules modules, GameInfra infra) {
        return FlowFactory.<PlayerSpawnContext>builder()
                .step(new RegisterOnlinePlayerStep())
                .step(new UpdateMobileRegionStep<>(modules.region()))
                .step(new NotifyPlayerSpawnStep(infra.eventBus()))
                .build();
    }
}
