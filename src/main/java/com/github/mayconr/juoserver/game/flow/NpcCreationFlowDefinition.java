package com.github.mayconr.juoserver.game.flow;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemModuleImpl;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.npc.flow.creation.*;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

public final class NpcCreationFlowDefinition {
    private NpcCreationFlowDefinition() {
    }

    public static Flow<NpcCreationContext> build(ItemModule itemModule,
                                                 AIModule aiModule,
                                                 EventBus eventBus,
                                                 RealmStorage storage,
                                                 SerialGenerator serialGenerator,
                                                 TemplateRegistry<String, NpcTemplate> npcTemplateRegistry,
                                                 World world) {
        return FlowFactory.<NpcCreationContext>builder()
                .step(new NpcSerialGenStep(100, serialGenerator))
                .step(new ResolveNpcTemplateStep(200, npcTemplateRegistry))
                .step(new CreateNpcEquippedItemsStep(300, itemModule))
                .step(new CreateNpcStep(400, storage))
                .step(new AttachAIStep(500, aiModule, world))
                .step(new NotifyNpcCreation(600, eventBus))
                .build();
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @RequiredArgsConstructor
    public static class NpcCreationContext extends SyncFlowContext {
        private final String templateName;
        private final Location location;

        private UONpc npc;
        private Integer serialId;
        private NpcTemplate template;
        private Map<Layer, Integer> equippedItems;
    }
}
