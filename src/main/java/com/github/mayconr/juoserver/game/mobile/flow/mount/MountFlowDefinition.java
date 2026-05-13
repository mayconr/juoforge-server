package com.github.mayconr.juoserver.game.mobile.flow.mount;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.flow.mount.creation.CreateMountItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.mount.equip.EquipMountItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.mount.conversion.RemoveMountNpcStep;
import com.github.mayconr.juoserver.game.mobile.flow.mount.validation.ValidateMountStep;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

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
