package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.flow.*;
import com.github.mayconr.juoserver.game.flow.DamageFlowDefinition.DamageContext;
import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition.DeathContext;
import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext;
import com.github.mayconr.juoserver.game.flow.EquipItemFlowDefinition.EquipItemContext;
import com.github.mayconr.juoserver.game.flow.ItemCreationFlowDefinition.ItemCreationContext;
import com.github.mayconr.juoserver.game.flow.NpcCreationFlowDefinition.NpcCreationContext;
import com.github.mayconr.juoserver.game.flow.NpcRemovalFlowDefinition.NpcRemovalContext;
import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.flow.UnequipItemFlowDefinition.UnequipItemContext;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartkitTemplate;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FlowRegistryFactory {
    // Modules
    private final ItemModule itemModule;
    private final MobileModule mobileModule;
    private final AIModule aiModule;
    private final MessageModule messageModule;

    // Utilities
    private final EventBus eventBus;
    private final RealmStorage storage;
    private final SerialGenerator serialGenerator;
    private final World world;
    private final GamePlaySettings settings;

    // Templates
    private final TemplateRegistry<String, NpcTemplate> npcTemplateByName;
    private final TemplateRegistry<String, ItemTemplate> itemTemplateByName;
    private final TemplateRegistry<Integer, ItemTemplate> itemTemplateByModelId;
    private final TemplateRegistry<BodyKey, BodyTemplate> bodyByBodyKey;
    private final TemplateRegistry<Integer, StartkitTemplate> startKitTemplateBySkillId;

    public FlowRegistry buildRegistry() {
        FlowRegistry registry = new DefaultFlowRegistry();

        registry.register(
                "Death",
                DeathFlowDefinition.build(itemModule, mobileModule, eventBus, storage),
                DeathContext.class
        );

        registry.register(
                "Damage",
                DamageFlowDefinition.build(mobileModule, eventBus),
                DamageContext.class
        );

        registry.register(
                "NpcCreation",
                NpcCreationFlowDefinition.build(
                        itemModule,
                        aiModule,
                        eventBus,
                        storage,
                        serialGenerator,
                        npcTemplateByName,
                        world
                ),
                NpcCreationContext.class
        );

        registry.register("NpcRemoval",
                NpcRemovalFlowDefinition.build(storage, eventBus, aiModule),
                NpcRemovalContext.class);

        registry.register("UnequipItem", UnequipItemFlowDefinition.build(storage, eventBus),
                UnequipItemContext.class);

        registry.register("EquipItem", EquipItemFlowDefinition.build(storage, eventBus), EquipItemContext.class);

        registry.register("ItemCreation", ItemCreationFlowDefinition.build(itemTemplateByName, itemTemplateByModelId, serialGenerator, storage, eventBus), ItemCreationContext.class);

        registry.register("DropItemInContainer", DropItemFlowDefinition.build(storage, eventBus, mobileModule, messageModule), DropItemContext.class);

        registry.register("PlayerCreationFlowDefinition", PlayerCreationFlowDefinition.build(storage, eventBus, bodyByBodyKey, settings, startKitTemplateBySkillId, itemModule, serialGenerator), PlayerCreationContext.class);
        return registry;
    }
}
