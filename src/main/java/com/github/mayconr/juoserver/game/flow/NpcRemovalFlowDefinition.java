package com.github.mayconr.juoserver.game.flow;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.npc.flow.removal.DetachNpcAiStep;
import com.github.mayconr.juoserver.game.npc.flow.removal.NotifyNpcRemoved;
import com.github.mayconr.juoserver.game.npc.flow.removal.RemoveNpcStep;
import com.github.mayconr.juoserver.game.npc.flow.removal.ValidateNpcRemoval;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

public class NpcRemovalFlowDefinition {

    private NpcRemovalFlowDefinition() {
    }

    public static Flow<NpcRemovalContext> build(RealmStorage storage, EventBus eventBus, AIModule aiModule) {
        return FlowFactory.<NpcRemovalContext>builder()
                .step(new ValidateNpcRemoval(100))
                .step(new RemoveNpcStep(200, storage))
                .step(new DetachNpcAiStep(300, aiModule))
                .step(new NotifyNpcRemoved(400, eventBus))
                .build();
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @RequiredArgsConstructor
    public static class NpcRemovalContext extends SyncFlowContext {
        private final UONpc npc;
    }
}
