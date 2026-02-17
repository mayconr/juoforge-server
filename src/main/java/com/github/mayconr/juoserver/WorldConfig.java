package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.combat.*;
import com.github.mayconr.juoserver.game.economy.*;
import com.github.mayconr.juoserver.game.economy.stock.RegionStockEntry;
import com.github.mayconr.juoserver.game.economy.stock.RegionStockPool;
import com.github.mayconr.juoserver.game.mobile.movement.MobileMovementRules;
import com.github.mayconr.juoserver.game.player.DefaultSessionManager;
import com.github.mayconr.juoserver.game.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.player.SessionManager;
import com.github.mayconr.juoserver.game.ui.*;
import com.github.mayconr.juoserver.game.world.DefaultWorld;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.ai.decision.NpcAI;
import com.github.mayconr.juoserver.game.ai.decision.NpcAiRegistry;
import com.github.mayconr.juoserver.game.ai.profile.BehaviorProfile;
import com.github.mayconr.juoserver.game.ai.profile.BehaviorProfileRegistry;
import com.github.mayconr.juoserver.game.ai.session.AISessionHandler;
import com.github.mayconr.juoserver.game.item.ItemDropHandler;
import com.github.mayconr.juoserver.game.item.ItemEquipHandler;
import com.github.mayconr.juoserver.game.item.ItemHandler;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.template.CachedItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.interaction.InteractionModule;
import com.github.mayconr.juoserver.game.interaction.action.ActionHandler;
import com.github.mayconr.juoserver.game.interaction.animation.AnimationHandler;
import com.github.mayconr.juoserver.game.mobile.movement.MovementHandler;
import com.github.mayconr.juoserver.game.interaction.speech.SpeechHandler;
import com.github.mayconr.juoserver.game.interaction.target.TargetHandler;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.MountHandler;
import com.github.mayconr.juoserver.game.mobile.npc.NpcHandler;
import com.github.mayconr.juoserver.game.mobile.npc.template.DefaultNpcTemplateRegistry;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.world.module.player.PlayerCreationHandler;
import com.github.mayconr.juoserver.game.world.module.player.PlayerLoginHandler;
import com.github.mayconr.juoserver.game.world.module.player.PlayerModule;
import com.github.mayconr.juoserver.game.world.module.player.PlayerRemovalHandler;
import com.github.mayconr.juoserver.game.skill.DefaultSkillSystem;
import com.github.mayconr.juoserver.game.skill.SkillHandler;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.ui.gump.DefaultGumpSystem;
import com.github.mayconr.juoserver.game.ui.gump.GumpSystem;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReaderSystem;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.gameloop.DefaultGameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyRegistry;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.region.MapRegionSystem;
import com.github.mayconr.juoserver.infrastructure.region.MapRegionSystemImpl;
import com.github.mayconr.juoserver.infrastructure.region.RegionTemplate;
import com.github.mayconr.juoserver.infrastructure.rng.DefaultRNG;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.storage.CachedRealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import io.netty.channel.group.ChannelGroup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@EnableConfigurationProperties(ServerProperties.class)
public class WorldConfig {

    // =========================================================================
    // Core Infrastructure
    // =========================================================================

    @Bean
    public EventBus eventBus() {
        return new DefaultEventBus();
    }

    @Bean
    public GameLoop gameLoop(ServerProperties properties) {
        final var gameLoop = new DefaultGameLoop(properties);
        Runtime.getRuntime().addShutdownHook(new Thread(gameLoop::stop));
        return gameLoop.start();
    }

    @Bean
    public RNG rng() {
        return new DefaultRNG();
    }

    // =========================================================================
    // Policy
    // =========================================================================

    @Bean
    public PolicyRegistry policyRegistry() {
        return new PolicyRegistry();
    }

    @Bean
    public PolicyService policyService(PolicyRegistry registry) {
        return new PolicyService(registry);
    }

    // =========================================================================
    // Item Use
    // =========================================================================

    @Bean
    public ItemUseRegistry itemUseRegistry() {
        return new ItemUseRegistry();
    }

    @Bean
    public ItemUseService itemUseService(ItemUseRegistry registry) {
        return new ItemUseService(registry);
    }

    // =========================================================================
    // Combat / Systems
    // =========================================================================

    @Bean
    public CombatSystem combatSystem(GameLoop gameLoop, ChannelGroup channelGroup) {
        final var combatSystem = new DefaultCombatSystem(channelGroup);
        gameLoop.addTask(combatSystem);
        return combatSystem;
    }

    @Bean
    public GumpSystem gumpSystem(EventBus eventBus) {
        return new DefaultGumpSystem(eventBus);
    }

    // =========================================================================
    // Templates
    // =========================================================================

    @Bean
    public TemplateLoader<NpcTemplate> npcTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/npcs"), NpcTemplate.class);
    }

    @Bean
    public TemplateLoader<RegionTemplate> regionTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/regions"), RegionTemplate.class);
    }

    @Bean
    public TemplateLoader<ItemTemplate> itemTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/items"), ItemTemplate.class);
    }

    @Bean
    public NpcTemplateRegistry npcTemplateRegistry(TemplateLoader<NpcTemplate> templateLoader) {
        return new DefaultNpcTemplateRegistry(templateLoader.load());
    }

    @Bean
    public ItemTemplateRegistry itemTemplateRegistry(TemplateLoader<ItemTemplate> itemTemplateLoader) {
        return new CachedItemTemplateRegistry(itemTemplateLoader.load());
    }

    // =========================================================================
    // Region
    // =========================================================================

    @Bean
    public MapRegionSystem mapRegionService(TemplateLoader<RegionTemplate> templateLoader) {
        return new MapRegionSystemImpl(templateLoader);
    }

    // =========================================================================
    // Economy
    // =========================================================================

    @Bean
    public EconomySystem economySystem(ItemTemplateRegistry itemTemplateRegistry) {
        var iron = itemTemplateRegistry.get("iron_ore");
        var gold = itemTemplateRegistry.get("gold_ore");
        var dull = itemTemplateRegistry.get("dull_copper_ore");
        Map<ItemTemplate, RegionStockEntry> britainEntries = Map.of(
                iron, new RegionStockEntry(iron, 1000, 0.8, 800),
                gold, new RegionStockEntry(gold, 200, 1.2, 150),
                dull, new RegionStockEntry(dull, 300, 1.2, 150)
        );
        Map<String, RegionStockPool> pools = Map.of("province-britannia", new RegionStockPool("province-britannia", britainEntries));
        return new DefaultEconomySystem(pools, new ScarcityBasedPricingStrategy());
    }

    // =========================================================================
    // Storage
    // =========================================================================

    @Bean
    public RealmStorage realmStorage(
            MobileStorage mobileStorage,
            ItemStorage itemStorage) {

        return new CachedRealmStorage(mobileStorage, itemStorage);
    }

    // =========================================================================
    // Sessions / World
    // =========================================================================

    @Bean
    public SessionManager sessionRegistry(WorldInternal world, EventBus eventBus) {
        return new DefaultSessionManager(world, eventBus);
    }

    @Bean
    public PlayerSessionFactory playerSessionFactory(ServerProperties properties) {
        return new PlayerSessionFactory(properties);
    }

    @Bean
    public SkillModule skillModule(ServerProperties properties, RNG rng, EventBus eventBus, RealmStorage storage) {
        final var skillSystem = new DefaultSkillSystem(properties, rng, eventBus);
        final var skillHandler = new SkillHandler(eventBus, storage);
        return  new SkillModule(skillHandler, skillSystem);
    }

    @Bean
    public AIModule aiModule(EventBus eventBus, List<NpcAI> npcAIs, List<BehaviorProfile> profiles) {
        final var aiFactory = new NpcAiRegistry(npcAIs);
        final var profileRegistry = new BehaviorProfileRegistry(profiles);
        final var aiSessionHandler = new AISessionHandler(eventBus, profileRegistry, aiFactory);
        return new AIModule(eventBus, aiSessionHandler);
    }

    @Bean
    public World worldSession(
            RealmStorage storage,
            EventBus eventBus,
            ItemTemplateRegistry itemTemplateRegistry,
            GameLoop gameLoop,
            SkillModule skillModule,
            ServerProperties properties,
            PolicyService policyService,
            NpcTemplateRegistry npcTemplateRegistry,
            ItemUseService itemUseService,
            CombatSystem combatSystem,
            GumpSystem gumpSystem,
            MapRegionSystem regionSystem,
            EconomySystem economySystem,
            AIModule aiModule) {

        final var serialGenerator = new SerialGenerator(storage);

        final var fileReaderSystem = new UOFileReaderSystem(properties);

        // modules
        final var itemHandler = new ItemHandler(serialGenerator, itemTemplateRegistry, storage, eventBus);
        final var itemDropHandler = new ItemDropHandler(eventBus, storage, policyService);
        final var itemEquipHandler = new ItemEquipHandler(storage, eventBus);
        final var itemModule = new ItemModule(itemHandler, itemDropHandler, itemEquipHandler);

        final var playerCreationHandler = new PlayerCreationHandler(serialGenerator, storage, itemTemplateRegistry, properties, policyService);
        final var playerLoginHandler = new PlayerLoginHandler(eventBus, storage);
        final var playerRemovalHandler = new PlayerRemovalHandler(storage, eventBus);
        final var playerModule = new PlayerModule(playerCreationHandler, playerLoginHandler, playerRemovalHandler);

        final var tooltipHandler = new TooltipHandler(eventBus, storage);
        final var doubleClickHandler = new DoubleClickHandler(eventBus, storage, itemUseService, policyService);
        final var singleClickHandler = new SingleClickHandler(storage);
        final var skillUIHandler = new SkillUIHandler(eventBus, storage);
        final var messageHandler = new MessageHandler(eventBus);
        final var statusHandler = new StatusHandler(eventBus, storage);
        var uiModule = new UIModule(tooltipHandler, doubleClickHandler, singleClickHandler, skillUIHandler, gumpSystem, messageHandler, statusHandler);

        final var vitalsService = new VitalsHandler(eventBus, properties);
        final var combatService = new CombatHandler(eventBus, combatSystem, storage);
        final var combatModule = new CombatModule(combatService, vitalsService);

        final var vendorHandler = new VendorHandler(eventBus, serialGenerator, economySystem);
        final var economyModule = new EconomyModule(vendorHandler, economySystem);

        final var speechHandler = new SpeechHandler(eventBus);
        final var targetHandler = new TargetHandler(eventBus);
        final var actionHandler = new ActionHandler(eventBus);
        final var animationService = new AnimationHandler(eventBus);
        final var interactionModule = new InteractionModule(speechHandler,  targetHandler, actionHandler, animationService);

        final var mountHandler = new MountHandler(eventBus, storage, policyService, itemTemplateRegistry, serialGenerator);
        final var npcHandler = new NpcHandler(npcTemplateRegistry, itemTemplateRegistry, serialGenerator, storage, eventBus);
        final var movementRules = new MobileMovementRules(storage);
        final var movementHandler = new MovementHandler(eventBus, movementRules);
        final var mobileModule = new MobileModule(mountHandler, npcHandler, movementHandler);

        final var world = new DefaultWorld(
                itemModule,
                playerModule,
                uiModule,
                combatModule,
                skillModule,
                economyModule,
                interactionModule,
                mobileModule,
                aiModule,
                eventBus,
                serialGenerator,
                storage,
                gameLoop,
                regionSystem,
                itemTemplateRegistry,
                fileReaderSystem,
                properties
        );

        world.initialize();

        gameLoop.addTask(new GameTask() {
            @Override
            public void execute(long currentTick, double delta) {
                world.update(delta);
            }

            @Override
            public boolean isDone() {
                return false;
            }
        });

        return world;
    }
}
