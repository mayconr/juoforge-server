package com.github.mayconr.juoserver.game.npc.flow.removal;

import com.github.mayconr.juoserver.game.npc.flow.removal.ai.DetachNpcAiStep;
import com.github.mayconr.juoserver.game.npc.flow.removal.notification.NotifyNpcRemoved;
import com.github.mayconr.juoserver.game.npc.flow.removal.removal.RemoveNpcStep;
import com.github.mayconr.juoserver.game.npc.flow.removal.validation.ValidateNpcRemoval;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class NpcRemovalFlowDefinition {

    private NpcRemovalFlowDefinition() {
    }

    public static Flow<NpcRemovalContext> build(GameModules modules, GameInfra infra) {
        return FlowFactory.<NpcRemovalContext>builder()
                .step(new ValidateNpcRemoval())
                .step(new RemoveNpcStep(infra.storage()))
                .step(new DetachNpcAiStep(modules.ai()))
                .step(new NotifyNpcRemoved(infra.eventBus()))
                .build();
    }

}
