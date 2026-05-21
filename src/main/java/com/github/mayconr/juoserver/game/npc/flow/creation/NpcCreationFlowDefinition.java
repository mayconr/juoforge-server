package com.github.mayconr.juoserver.game.npc.flow.creation;

import com.github.mayconr.juoserver.game.npc.flow.creation.ai.AttachAIStep;
import com.github.mayconr.juoserver.game.npc.flow.creation.build.CreateNpcEquippedItemsStep;
import com.github.mayconr.juoserver.game.npc.flow.creation.creation.CreateNpcStep;
import com.github.mayconr.juoserver.game.npc.flow.creation.identity.NpcSerialGenStep;
import com.github.mayconr.juoserver.game.npc.flow.creation.notification.NotifyNpcCreation;
import com.github.mayconr.juoserver.game.npc.flow.creation.resolve.ResolveNpcTemplateStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class NpcCreationFlowDefinition {
    private NpcCreationFlowDefinition() {
    }

    public static Flow<NpcCreationContext> build(GameModules modules, GameInfra infra, GameTemplates templates) {
        return FlowFactory.<NpcCreationContext>builder()
                .step(new NpcSerialGenStep(infra.serialGenerator()))
                .step(new ResolveNpcTemplateStep(templates.npcByName()))
                .step(new CreateNpcEquippedItemsStep(modules.item()))
                .step(new CreateNpcStep(infra.storage()))
                .step(new AttachAIStep(modules.ai()))
                .step(new NotifyNpcCreation(infra.eventBus()))
                .build();
    }

}
