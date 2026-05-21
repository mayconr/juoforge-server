package com.github.mayconr.juoserver.game.mobile.flow.mount;

import com.github.mayconr.juoserver.game.mobile.flow.mount.creation.CreateMountItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.mount.equip.EquipMountItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.mount.conversion.RemoveMountNpcStep;
import com.github.mayconr.juoserver.game.mobile.flow.mount.validation.ValidateMountStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class MountFlowDefinition {

    public static Flow<MountContext> build(GameModules modules, GameTemplates templates) {
        return FlowFactory.<MountContext>builder()
                .step(new ValidateMountStep(templates.mountByNpcName()))
                .step(new CreateMountItemStep(modules.item(), templates.mountByNpcName()))
                .step(new RemoveMountNpcStep(modules.npc()))
                .step(new EquipMountItemStep(modules.mobile()))
                .build();
    }

}
