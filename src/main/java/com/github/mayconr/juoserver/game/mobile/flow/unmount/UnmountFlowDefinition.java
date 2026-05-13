package com.github.mayconr.juoserver.game.mobile.flow.unmount;

import com.github.mayconr.juoserver.game.mobile.flow.unmount.creation.CreateMountNpcStep;
import com.github.mayconr.juoserver.game.mobile.flow.unmount.load.LoadMountItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unmount.load.LoadMountNpcNameStep;
import com.github.mayconr.juoserver.game.mobile.flow.unmount.state.UnequipMountItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unmount.validation.ValidateUnmountStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class UnmountFlowDefinition {
    private UnmountFlowDefinition() {
    }

    public static Flow<UnmountContext> build(GameModules modules, GameInfra infra, GameTemplates templates) {
        return FlowFactory.<UnmountContext>builder()
                .step(new ValidateUnmountStep())
                .step(new LoadMountItemStep(infra.storage()))
                .step(new UnequipMountItemStep(infra.storage(), infra.eventBus()))
                .step(new LoadMountNpcNameStep(templates.mountByItemName()))
                .step(new CreateMountNpcStep(modules.npc()))
                .build();
    }

}
