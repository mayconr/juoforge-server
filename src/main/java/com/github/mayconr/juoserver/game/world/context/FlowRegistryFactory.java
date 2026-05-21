package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.ai.definition.PassiveAnimalAIContext;
import com.github.mayconr.juoserver.game.ai.definition.PassiveAnimalAIDefinition;
import com.github.mayconr.juoserver.game.ai.definition.VendorAIContext;
import com.github.mayconr.juoserver.game.ai.definition.VendorAIDefinition;
import com.github.mayconr.juoserver.game.damage.flow.damage.DamageContext;
import com.github.mayconr.juoserver.game.damage.flow.damage.DamageFlowDefinition;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetContext;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetFlowDefinition;
import com.github.mayconr.juoserver.game.interaction.flow.target.send.SendTargetContext;
import com.github.mayconr.juoserver.game.interaction.flow.target.send.SendTargetFlowDefinition;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationFlowDefinition;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemFlowDefinition;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.mount.MountContext;
import com.github.mayconr.juoserver.game.mobile.flow.mount.MountFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.resync.ResyncContext;
import com.github.mayconr.juoserver.game.mobile.flow.resync.ResyncFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportContext;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.flow.unmount.UnmountContext;
import com.github.mayconr.juoserver.game.mobile.flow.unmount.UnmountFlowDefinition;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationFlowDefinition;
import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalContext;
import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalFlowDefinition;
import com.github.mayconr.juoserver.game.player.flow.creation.PlayerCreationContext;
import com.github.mayconr.juoserver.game.player.flow.creation.PlayerCreationFlowDefinition;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartKitTemplate;
import com.github.mayconr.juoserver.game.skill.flow.use.UseSkillContext;
import com.github.mayconr.juoserver.game.skill.flow.use.UseSkillFlowDefinition;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class FlowRegistryFactory {

    private final GameModules modules;
    private final GameInfra infra;
    private final GameTemplates templates;

    @Builder
    public record GameModules(
            ItemModule item,
            MobileModule mobile,
            AIModule ai,
            MessageModule message,
            NpcModule npc
    ) {}

    @Builder
    public record GameInfra(
            EventBus eventBus,
            RealmStorage storage,
            SerialGenerator serialGenerator,
            GamePlaySettings settings,
            UOFileReader fileReader
    ) {}

    @Builder
    public record GameTemplates(
            TemplateRegistry<String, NpcTemplate> npcByName,

            TemplateRegistry<String, ItemTemplate> itemByName,
            TemplateRegistry<Integer, ItemTemplate> itemByModelId,

            TemplateRegistry<BodyKey, BodyTemplate> bodyByKey,

            TemplateRegistry<Integer, StartKitTemplate> startKitBySkillId,

            TemplateRegistry<String, MountTemplate> mountByNpcName,
            TemplateRegistry<String, MountTemplate> mountByItemName
    ) {}

    public FlowRegistry buildRegistry() {
        FlowRegistry registry = new DefaultFlowRegistry();
        registry.register(DeathFlowDefinition.class.getSimpleName(), DeathFlowDefinition.build(modules, infra), DeathContext.class);
        registry.register(DamageFlowDefinition.class.getSimpleName(), DamageFlowDefinition.build(modules, infra), DamageContext.class);
        registry.register(NpcCreationFlowDefinition.class.getSimpleName(), NpcCreationFlowDefinition.build(modules, infra, templates), NpcCreationContext.class);
        registry.register(NpcRemovalFlowDefinition.class.getSimpleName(), NpcRemovalFlowDefinition.build(modules, infra), NpcRemovalContext.class);
        registry.register(UnequipItemFlowDefinition.class.getSimpleName(), UnequipItemFlowDefinition.build(infra), UnequipItemContext.class);
        registry.register(EquipItemFlowDefinition.class.getSimpleName(), EquipItemFlowDefinition.build(infra), EquipItemContext.class);
        registry.register(ItemCreationFlowDefinition.class.getSimpleName(), ItemCreationFlowDefinition.build(infra, templates), ItemCreationContext.class);
        registry.register(DropItemFlowDefinition.class.getSimpleName(), DropItemFlowDefinition.build(modules, infra), DropItemContext.class);
        registry.register(PlayerCreationFlowDefinition.class.getSimpleName(), PlayerCreationFlowDefinition.build(modules, infra, templates), PlayerCreationContext.class);
        registry.register(UnmountFlowDefinition.class.getSimpleName(), UnmountFlowDefinition.build(modules, infra, templates), UnmountContext.class);
        registry.register(MountFlowDefinition.class.getSimpleName(), MountFlowDefinition.build(modules, templates), MountContext.class);
        registry.register(PassiveAnimalAIDefinition.class.getSimpleName(), PassiveAnimalAIDefinition.build(), PassiveAnimalAIContext.class);
        registry.register(VendorAIDefinition.class.getSimpleName(), VendorAIDefinition.build(), VendorAIContext.class);
        registry.register(SendTargetFlowDefinition.class.getSimpleName(), SendTargetFlowDefinition.build(modules, infra), SendTargetContext.class);
        registry.register(ResolveTargetFlowDefinition.class.getSimpleName(), ResolveTargetFlowDefinition.build(modules, infra), ResolveTargetContext.class);
        registry.register(UseSkillFlowDefinition.class.getSimpleName(), UseSkillFlowDefinition.build(modules, infra), UseSkillContext.class);
        registry.register(TeleportFlowDefinition.class.getSimpleName(), TeleportFlowDefinition.build(infra), TeleportContext.class);
        registry.register(MovementFlowDefinition.class.getSimpleName(), MovementFlowDefinition.build(infra), MovementContext.class);
        registry.register(ResyncFlowDefinition.class.getSimpleName(), ResyncFlowDefinition.build(infra), ResyncContext.class);
        return registry;
    }
}
