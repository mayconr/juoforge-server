package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.game.loot.LootModule;
import com.github.mayconr.juoserver.game.ui.flow.doubleclick.DoubleClickContext;
import com.github.mayconr.juoserver.game.ui.flow.skill.SkillGumpContext;
import com.github.mayconr.juoserver.game.ui.flow.singleclick.SingleClickContext;
import com.github.mayconr.juoserver.game.ui.flow.singleclick.SingleClickFlowDefinition;
import com.github.mayconr.juoserver.game.ui.flow.skill.SkillGumpFlowDefinition;
import com.github.mayconr.juoserver.game.ui.UIModule;
import com.github.mayconr.juoserver.game.ui.flow.tooltip.TooltipContext;
import com.github.mayconr.juoserver.game.ui.flow.tooltip.TooltipFlowDefinition;
import com.github.mayconr.juoserver.game.ui.flow.status.StatusGumpContext;
import com.github.mayconr.juoserver.game.ui.flow.status.StatusGumpFlowDefinition;
import com.github.mayconr.juoserver.game.ui.flow.paperdoll.OpenPaperdollContext;
import com.github.mayconr.juoserver.game.ui.flow.paperdoll.OpenPaperdollFlowDefinition;
import com.github.mayconr.juoserver.game.ui.flow.doubleclick.DoubleClickFlowDefinition;
import com.github.mayconr.juoserver.game.ui.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.game.item.flow.open.OpenContainerContext;
import com.github.mayconr.juoserver.game.item.flow.open.OpenContainerFlowDefinition;
import com.github.mayconr.juoserver.game.loot.flow.materialize.MaterializeLootContext;
import com.github.mayconr.juoserver.game.loot.flow.materialize.MaterializeLootFlowDefinition;
import com.github.mayconr.juoserver.game.loot.LootPolicy;
import com.github.mayconr.juoserver.game.loot.flow.generate.GenerateLootContext;
import com.github.mayconr.juoserver.game.loot.flow.generate.GenerateLootFlowDefinition;

import com.github.mayconr.juoserver.game.economy.VendorHandler;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.economy.flow.begin.BeginVendorPurchaseContext;
import com.github.mayconr.juoserver.game.economy.flow.begin.BeginVendorPurchaseFlowDefinition;
import com.github.mayconr.juoserver.game.economy.flow.purchase.VendorPurchaseContext;
import com.github.mayconr.juoserver.game.economy.flow.purchase.VendorPurchaseFlowDefinition;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.vitals.flow.regen.RegenContext;
import com.github.mayconr.juoserver.game.vitals.flow.regen.RegenFlowDefinition;
import com.github.mayconr.juoserver.game.combat.progression.CombatSkillGainPolicy;
import com.github.mayconr.juoserver.game.combat.progression.DefaultCombatSkillGainPolicy;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellContext;
import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellFlowDefinition;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.game.spell.flow.open.OpenSpellBookContext;
import com.github.mayconr.juoserver.game.spell.flow.open.OpenSpellBookFlowDefinition;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.rng.DefaultRNG;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.ai.definition.animal.PassiveAnimalAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIDefinition;
import com.github.mayconr.juoserver.game.ai.definition.animal.PassiveAnimalAIDefinition;
import com.github.mayconr.juoserver.game.ai.definition.vendor.VendorAIContext;
import com.github.mayconr.juoserver.game.ai.definition.vendor.VendorAIDefinition;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionFlow;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationFlow;
import com.github.mayconr.juoserver.game.damage.DamageModule;
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
import com.github.mayconr.juoserver.game.mobile.flow.resurrection.ResurrectionContext;
import com.github.mayconr.juoserver.game.mobile.flow.resurrection.ResurrectionFlowDefinition;
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
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationFlowDefinition;
import com.github.mayconr.juoserver.game.npc.stats.NpcStatsResolver;
import com.github.mayconr.juoserver.game.npc.ai.NpcAIProfileResolver;
import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalContext;
import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalFlowDefinition;
import com.github.mayconr.juoserver.game.player.flow.creation.PlayerCreationContext;
import com.github.mayconr.juoserver.game.player.flow.deletion.DeletePlayerContext;
import com.github.mayconr.juoserver.game.player.flow.deletion.DeletePlayerFlowDefinition;
import com.github.mayconr.juoserver.game.player.flow.creation.PlayerCreationFlowDefinition;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartKitTemplate;
import com.github.mayconr.juoserver.game.skill.flow.use.UseSkillContext;
import com.github.mayconr.juoserver.game.skill.flow.lock.SendSkillsLockContext;
import com.github.mayconr.juoserver.game.skill.flow.lock.SendSkillsLockFlowDefinition;
import com.github.mayconr.juoserver.game.skill.flow.set.SetSkillContext;
import com.github.mayconr.juoserver.game.skill.flow.set.SetSkillFlowDefinition;
import com.github.mayconr.juoserver.game.skill.flow.use.UseSkillFlowDefinition;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.serialization.DataMapper;
import com.github.mayconr.juoserver.infrastructure.serialization.JacksonDataMapper;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.game.region.RegionModule;
import com.github.mayconr.juoserver.game.region.flow.transition.RegionTransitionContext;
import com.github.mayconr.juoserver.game.region.flow.transition.RegionTransitionFlowDefinition;
import com.github.mayconr.juoserver.game.ai.flow.deactivation.DeactivateRegionContext;
import com.github.mayconr.juoserver.game.ai.flow.activation.ActivateRegionContext;
import com.github.mayconr.juoserver.game.player.flow.spawn.PlayerSpawnContext;
import com.github.mayconr.juoserver.game.player.flow.spawn.PlayerSpawnFlowDefinition;
import com.github.mayconr.juoserver.game.ai.flow.activation.ActivateRegionFlowDefinition;
import com.github.mayconr.juoserver.game.ai.flow.deactivation.DeactivateRegionFlowDefinition;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;
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
            RegionModule region,
            ItemModule item,
            MobileModule mobile,
            AIModule ai,
            MessageModule message,
            NpcModule npc,
            DamageModule damage,
            LootModule loot,
            SkillModule skill,
            UIModule ui
    ) {}

    @Builder
    public record GameInfra(
            RegionSystem regionSystem,
            VendorHandler vendorHandler,
            Wallet wallet,
            SpellCastRegistry spellCastRegistry,
            EventBus eventBus,
            RealmStorage storage,
            SerialGenerator serialGenerator,
            GamePlaySettings settings,
            UOFileReader fileReader,
            RNG rng,
            CombatSkillGainPolicy combatSkillGainPolicy,
            LootPolicy lootPolicy,
            DataMapper dataMapper,
            DoubleClickPolicy doubleClickPolicy
    ) {
        public GameInfra {
            doubleClickPolicy = doubleClickPolicy == null ? DoubleClickPolicy.allowAll() : doubleClickPolicy;
            dataMapper = dataMapper == null ? new JacksonDataMapper() : dataMapper;
            rng = rng == null ? new DefaultRNG() : rng;
            lootPolicy = lootPolicy == null ? LootPolicy.none() : lootPolicy;
            combatSkillGainPolicy = combatSkillGainPolicy == null
                    ? new DefaultCombatSkillGainPolicy() : combatSkillGainPolicy;
        }
    }

    @Builder
    public record GameTemplates(
            TemplateRegistry<String, SpellTemplate> spellByKey,
            TemplateRegistry<String, NpcTemplate> npcByName,
            NpcStatsResolver npcStatsResolver,
            NpcAIProfileResolver npcAIProfileResolver,

            TemplateRegistry<String, ItemTemplate> itemByName,
            TemplateRegistry<Integer, ItemTemplate> itemByModelId,

            TemplateRegistry<BodyKey, BodyTemplate> bodyByKey,

            TemplateRegistry<Integer, StartKitTemplate> startKitBySkillId,

            TemplateRegistry<String, MountTemplate> mountByNpcName,
            TemplateRegistry<String, MountTemplate> mountByItemName
    ) {}

    public FlowRegistry buildRegistry() {
        FlowRegistry registry = new DefaultFlowRegistry();
        registry.register(PlayerSpawnFlowDefinition.class.getSimpleName(),
                PlayerSpawnFlowDefinition.build(modules, infra), PlayerSpawnContext.class);
        registry.register(ActivateRegionFlowDefinition.class.getSimpleName(),
                ActivateRegionFlowDefinition.build(modules.ai()), ActivateRegionContext.class);
        registry.register(DeactivateRegionFlowDefinition.class.getSimpleName(),
                DeactivateRegionFlowDefinition.build(modules.region(), modules.ai()),
                DeactivateRegionContext.class);
        registry.register(RegionTransitionFlowDefinition.class.getSimpleName(),
                RegionTransitionFlowDefinition.build(infra.regionSystem(), infra.eventBus(), modules.region(), modules.ai()),
                RegionTransitionContext.class);
        registry.register(SetSkillFlowDefinition.class.getSimpleName(),
                SetSkillFlowDefinition.build(infra.eventBus()), SetSkillContext.class);
        registry.register(SendSkillsLockFlowDefinition.class.getSimpleName(),
                SendSkillsLockFlowDefinition.build(infra.eventBus()), SendSkillsLockContext.class);
        registry.register(TooltipFlowDefinition.class.getSimpleName(),
                TooltipFlowDefinition.build(infra.eventBus(), infra.storage()), TooltipContext.class);
        registry.register(SingleClickFlowDefinition.class.getSimpleName(),
                SingleClickFlowDefinition.build(infra.storage(), infra.eventBus()), SingleClickContext.class);
        registry.register(StatusGumpFlowDefinition.class.getSimpleName(),
                StatusGumpFlowDefinition.build(infra.eventBus(), infra.storage()), StatusGumpContext.class);
        registry.register(SkillGumpFlowDefinition.class.getSimpleName(),
                SkillGumpFlowDefinition.build(infra.eventBus(), infra.storage()), SkillGumpContext.class);
        registry.register(DoubleClickFlowDefinition.class.getSimpleName(),
                DoubleClickFlowDefinition.build(infra.storage(), infra.doubleClickPolicy(), modules.item()), DoubleClickContext.class);
        registry.register(OpenPaperdollFlowDefinition.class.getSimpleName(),
                OpenPaperdollFlowDefinition.build(infra.eventBus(), infra.storage()), OpenPaperdollContext.class);
        registry.register(OpenContainerFlowDefinition.class.getSimpleName(),
                OpenContainerFlowDefinition.build(infra.storage(), infra.eventBus(), modules.loot()), OpenContainerContext.class);
        registry.register(MaterializeLootFlowDefinition.class.getSimpleName(),
                MaterializeLootFlowDefinition.build(modules.item(), infra.dataMapper()), MaterializeLootContext.class);
        registry.register(GenerateLootFlowDefinition.class.getSimpleName(),
                GenerateLootFlowDefinition.build(infra.lootPolicy(), infra.rng(), templates.itemByName(), templates.itemByModelId(), infra.dataMapper()), GenerateLootContext.class);
        registry.register(RegenFlowDefinition.class.getSimpleName(),
                RegenFlowDefinition.build(infra.settings(), infra.eventBus()), RegenContext.class);
        registry.register(ResurrectionFlowDefinition.class.getSimpleName(),
                ResurrectionFlowDefinition.build(infra.eventBus()), ResurrectionContext.class);
        registry.register(DeletePlayerFlowDefinition.class.getSimpleName(),
                DeletePlayerFlowDefinition.build(infra.storage()), DeletePlayerContext.class);
        registry.register(VendorPurchaseFlowDefinition.class.getSimpleName(),
                VendorPurchaseFlowDefinition.build(infra.vendorHandler(), infra.wallet(), modules.item(), infra.eventBus()), VendorPurchaseContext.class);
        registry.register(BeginVendorPurchaseFlowDefinition.class.getSimpleName(),
                BeginVendorPurchaseFlowDefinition.build(infra.vendorHandler(), infra.eventBus()), BeginVendorPurchaseContext.class);
        registry.register(CastSpellFlowDefinition.class.getSimpleName(),
                CastSpellFlowDefinition.build(templates.spellByKey(), infra.spellCastRegistry()), CastSpellContext.class);
        registry.register(OpenSpellBookFlowDefinition.class.getSimpleName(), OpenSpellBookFlowDefinition.build(infra), OpenSpellBookContext.class);
        registry.register(DeathFlowDefinition.class.getSimpleName(), DeathFlowDefinition.build(modules, infra, templates), DeathContext.class);
        registry.register(DamageFlowDefinition.class.getSimpleName(), DamageFlowDefinition.build(modules, infra), DamageContext.class);
        registry.register(NpcCreationFlowDefinition.class.getSimpleName(), NpcCreationFlowDefinition.build(modules, infra, templates), NpcCreationContext.class);
        registry.register(NpcRemovalFlowDefinition.class.getSimpleName(), NpcRemovalFlowDefinition.build(modules, infra), NpcRemovalContext.class);
        registry.register(UnequipItemFlowDefinition.class.getSimpleName(), UnequipItemFlowDefinition.build(infra), UnequipItemContext.class);
        registry.register(EquipItemFlowDefinition.class.getSimpleName(), EquipItemFlowDefinition.build(infra), EquipItemContext.class);
        registry.register(ItemCreationFlowDefinition.class.getSimpleName(), ItemCreationFlowDefinition.build(infra, templates), ItemCreationContext.class);
        registry.register(DropItemFlowDefinition.class.getSimpleName(), DropItemFlowDefinition.build(modules, infra), DropItemContext.class);
        registry.register(PlayerCreationFlowDefinition.class.getSimpleName(), PlayerCreationFlowDefinition.build(modules, infra, templates), PlayerCreationContext.class);
        registry.register(UnmountFlowDefinition.class.getSimpleName(), UnmountFlowDefinition.build(modules, infra, templates), UnmountContext.class);
        registry.register(MountFlowDefinition.class.getSimpleName(), MountFlowDefinition.build(modules, infra, templates), MountContext.class);
        registry.register(PassiveAnimalAIDefinition.class.getSimpleName(), PassiveAnimalAIDefinition.build(), PassiveAnimalAIContext.class);
        registry.register(VendorAIDefinition.class.getSimpleName(), VendorAIDefinition.build(), VendorAIContext.class);
        registry.register(AggressiveAIDefinition.class.getSimpleName(), AggressiveAIDefinition.build(), AggressiveAIContext.class);
        registry.register(SendTargetFlowDefinition.class.getSimpleName(), SendTargetFlowDefinition.build(modules, infra), SendTargetContext.class);
        registry.register(ResolveTargetFlowDefinition.class.getSimpleName(), ResolveTargetFlowDefinition.build(modules, infra), ResolveTargetContext.class);
        registry.register(UseSkillFlowDefinition.class.getSimpleName(), UseSkillFlowDefinition.build(modules, infra), UseSkillContext.class);
        registry.register(TeleportFlowDefinition.class.getSimpleName(), TeleportFlowDefinition.build(modules, infra), TeleportContext.class);
        registry.register(MovementFlowDefinition.class.getSimpleName(), MovementFlowDefinition.build(modules, infra), MovementContext.class);
        registry.register(ResyncFlowDefinition.class.getSimpleName(), ResyncFlowDefinition.build(infra), ResyncContext.class);
        registry.register(CombatExecutionFlow.class.getSimpleName(), CombatExecutionFlow.build(modules, infra), CombatExecutionContext.class);
        registry.register(CombatPreparationFlow.class.getSimpleName(), CombatPreparationFlow.build(infra), CombatPreparationContext.class);
        return registry;
    }
}
