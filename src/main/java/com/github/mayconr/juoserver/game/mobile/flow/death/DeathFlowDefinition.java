package com.github.mayconr.juoserver.game.mobile.flow.death;

import com.github.mayconr.juoserver.game.mobile.flow.death.creation.CorpseCreationStep;
import com.github.mayconr.juoserver.game.mobile.flow.death.loot.GenerateDeathLootStep;
import com.github.mayconr.juoserver.game.mobile.flow.death.notification.NotifyDeathStep;
import com.github.mayconr.juoserver.game.mobile.flow.death.spawn.SpawnMobileMountStep;
import com.github.mayconr.juoserver.game.mobile.flow.death.state.UpdateMobileDeathStatusStep;
import com.github.mayconr.juoserver.game.mobile.flow.death.transfer.MoveItemsToCorpseStep;
import com.github.mayconr.juoserver.game.mobile.flow.death.validation.ValidateDeathStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class DeathFlowDefinition {
    private DeathFlowDefinition() {
    }

    public static Flow<DeathContext> build(FlowRegistryFactory.GameModules modules, FlowRegistryFactory.GameInfra infra,
                                         FlowRegistryFactory.GameTemplates templates) {
        return FlowFactory.<DeathContext>builder()
                .step(new ValidateDeathStep())
                .step(new CorpseCreationStep(modules.item()))
                .step(new MoveItemsToCorpseStep(modules.mobile(), modules.item(), infra.storage()))
                .step(new UpdateMobileDeathStatusStep())
                .step(new SpawnMobileMountStep(modules.npc(), templates.mountByItemName()))
                .step(new GenerateDeathLootStep(modules.loot()))
                .step(new NotifyDeathStep(infra.eventBus()))
                .build();
    }

}
