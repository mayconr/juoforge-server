package com.github.mayconr.juoserver.game.mobile.flow.mount;

import com.github.mayconr.juoserver.game.mobile.flow.mount.creation.CreateMountItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.mount.equip.EquipMountItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.mount.conversion.RemoveMountNpcStep;
import com.github.mayconr.juoserver.game.mobile.flow.mount.validation.ValidateMountStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class MountFlowDefinition {

    public static Flow<MountContext> build(GameModules modules, GameInfra infra, GameTemplates templates) {
        // TODO Preserve mount integrity on failure: the NPC is removed before equipping the item.
        // Add rollback or defer NPC removal so later failures cannot leave the mount partially converted.
        return FlowFactory.<MountContext>builder()
                .step(new ValidateMountStep(templates.mountByNpcName(), infra.settings().mobile().mountRange()))
                .step(new CreateMountItemStep(modules.item(), templates.mountByNpcName()))
                .step(new RemoveMountNpcStep(modules.npc()))
                .step(new EquipMountItemStep(modules.mobile()))
                .build();
    }

}
